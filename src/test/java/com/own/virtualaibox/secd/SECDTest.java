package com.own.virtualaibox.secd;

import com.own.virtualaibox.secd.value.ClosureValue;
import com.own.virtualaibox.secd.value.InfiniteValue;
import com.own.virtualaibox.secd.value.IntValue;
import com.own.virtualaibox.secd.value.OpValue;
import com.own.virtualaibox.secd.value.Value;
import com.own.virtualaibox.secd.value.VarValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * P0 移植验证：程序化构造 λ 表达式，验证 SECD 归约正确性。
 *
 * <p>用例对应 lambdaexpr {@code LambdaExprTest} 中的代表性场景：
 * 算术柯里化、一元原语、闭包应用、闭包环境捕获、纯 λ 丘奇数、发散检测。
 * 不依赖 ANTLR —— 指令树直接用 {@code InstApp/InstLam/...} 手工构造。</p>
 */
class SECDTest {

    // ---------- 指令构造助手 ----------

    private static InstConst c(int v) {
        return new InstConst(new IntValue(v));
    }

    private static InstConst op(String name) {
        return new InstConst(new OpValue(name));
    }

    private static InstVar v(String name) {
        return new InstVar(name);
    }

    private static InstLam lam(String arg, Instruction body) {
        return new InstLam(arg, body);
    }

    private static InstApp app(Instruction rator, Instruction rand) {
        return new InstApp(rator, rand);
    }

    private static Reduction eval(Instruction program) {
        return new SECD().run(new MachineState(), program);
    }

    private static int intResult(Reduction r) {
        assertInstanceOf(IntValue.class, r.result(), "expected IntValue but got: " + r.result());
        return ((IntValue) r.result()).val;
    }

    // ---------- 用例 ----------

    @Test
    void addCurrying() {
        // ((add 10) 20) -> 30
        Reduction r = eval(app(app(op("add"), c(10)), c(20)));
        assertEquals(30, intResult(r));
        assertTrue(r.effects().isEmpty(), "纯算术不应产生副作用");
    }

    @Test
    void mulCurrying() {
        // ((mul 3) 4) -> 12
        assertEquals(12, intResult(eval(app(app(op("mul"), c(3)), c(4)))));
    }

    @Test
    void unarySqr() {
        // (sqr 10) -> 100
        assertEquals(100, intResult(eval(app(op("sqr"), c(10)))));
    }

    @Test
    void unarySucc() {
        // (succ 10) -> 11
        assertEquals(11, intResult(eval(app(op("succ"), c(10)))));
    }

    @Test
    void closureApply() {
        // ((lambda x.((add x) 5)) 10) -> 15
        assertEquals(15, intResult(eval(app(lam("x", app(app(op("add"), v("x")), c(5))), c(10)))));
    }

    @Test
    void nestedClosures() {
        // ((lambda a.((lambda b.((add ((mul a) b)) ((mul b) a))) ((add 5) 7))) 2)
        //   a=2, b=12 -> add(mul 2 12)(mul 12 2) = 24 + 24 = 48
        Instruction inner = app(
                app(op("add"),
                        app(app(op("mul"), v("a")), v("b"))),
                app(app(op("mul"), v("b")), v("a")));
        Instruction expr = app(
                lam("a", app(lam("b", inner), app(app(op("add"), c(5)), c(7)))),
                c(2));
        assertEquals(48, intResult(eval(expr)));
    }

    @Test
    void closureCapturesEnv() {
        // (((lambda y.(lambda x.(add x y))) 3) 4) -> add 4 3 = 7
        // 验证内层闭包捕获外层 y 的绑定
        Instruction expr = app(
                app(lam("y", lam("x", app(app(op("add"), v("x")), v("y")))), c(3)),
                c(4));
        assertEquals(7, intResult(eval(expr)));
    }

    @Test
    void identityOnVar() {
        // ((lambda x.x) y) -> y（未绑定变量返回 VarValue）
        Reduction r = eval(app(lam("x", v("x")), v("y")));
        assertInstanceOf(VarValue.class, r.result());
        assertEquals("y", r.result().toString());
    }

    @Test
    void identityOnInt() {
        // ((lambda x.x) 20) -> 20
        assertEquals(20, intResult(eval(app(lam("x", v("x")), c(20)))));
    }

    @Test
    void churchSuccAppliedTwice() {
        // (((lambda f.lambda x.(f (f x))) succ) 1) -> succ(succ 1) = 3
        Instruction twice = lam("f", lam("x", app(v("f"), app(v("f"), v("x")))));
        assertEquals(3, intResult(eval(app(app(twice, op("succ")), c(1)))));
    }

    @Test
    void higherOrderAdd() {
        // (((lambda x.(lambda y.(add x y))) 3) 4) -> 7
        assertEquals(7, intResult(eval(
                app(app(lam("x", lam("y", app(app(op("add"), v("x")), v("y")))), c(3)), c(4)))));
    }

    @Test
    void nestedSucc() {
        // ((lambda x.(succ (succ x))) 5) -> 7
        assertEquals(7, intResult(eval(
                app(lam("x", app(op("succ"), app(op("succ"), v("x")))), c(5)))));
    }

    @Test
    void selfApplicationPartial() {
        // (((lambda x.(x x)) sqr) 4)
        // (sqr sqr) 是非整数实参的一元应用 -> 得到延迟闭包 λx.(sqr x)，
        // 再应用到 4 -> sqr(4) = 16（不是 256，因 x 在闭包应用时被覆盖）
        Instruction selfApp = lam("x", app(v("x"), v("x")));
        assertEquals(16, intResult(eval(app(app(selfApp, op("sqr")), c(4)))));
    }

    @Test
    void shadowingReturnsClosure() {
        // lambda x.lambda x.x -> 闭包（内层 x 遮蔽外层）
        Reduction r = eval(lam("x", lam("x", v("x"))));
        assertInstanceOf(ClosureValue.class, r.result());
        assertEquals("(lambda x.(lambda x.x))", r.result().toString());
    }

    @Test
    void infiniteReductionDetected() {
        // (lambda x.(x x)) (lambda x.(x x)) -> 发散，应返回 InfiniteValue
        Instruction omega = app(lam("x", app(v("x"), v("x"))), lam("x", app(v("x"), v("x"))));
        Reduction r = eval(omega);
        assertTrue(r.infiniteReduction(), "应检测到无限归约");
        assertInstanceOf(InfiniteValue.class, r.result());
        assertTrue(r.result().toString().startsWith("inf"));
    }

    @Test
    void stateIsTerminatedAfterRun() {
        MachineState state = new MachineState();
        new SECD().run(state, app(app(op("add"), c(10)), c(20)));
        assertTrue(state.isTerminated(), "运行结束后 C/D 栈应为空");
    }
}
