grammar BehaviorDSL;

/*
 * P5 行为 DSL（ANTLR 4.13）。对应 docs/secd-fusion-design.md §10，语法 v0.2：
 *
 *   program   := topLevel (';'? topLevel)* ';'? EOF    # def 用 ';' 或换行分隔
 *   topLevel  := NAME '=' expr        # def：命名行为（onMeet 等 λ 行为）
 *              | 'plan' '=' expr      # plan：入口程序（内联，直接用 dx/dy）
 *   expr      := lambda | let | ifExpr | seq | application | atom | '(' expr ')'
 *   application := '(' callArg callArg* ')'   # (f a b)，零参 (f)
 *               | NAME callArg+               # 并列 f a b
 *   callArg   := atom | '(' expr ')'          # 参数：原子或括号表达式（杜绝并列吞并）
 *   atom      := INT | STRING | NAME | direction
 *
 * 说明（相对 §10 草案的收敛）：
 *   - 应用参数限定为"原子或括号表达式"（callArg），括号应用 (f a b) 无歧义地
 *     左结合展开为 ((f a) b)；
 *   - (f) 为零参调用（感知原语 closest/self 用），编译为"应用一次到 unit"；
 *     （P5 的 expr+ 要求至少 1 个参数，无法表达零参调用，P6 改 callArg* 支持。）
 *   - move 在运行时是柯里化二元原语，故应用改为变参形式 f a b == ((f a) b)，
 *     括号 (f a b) 为等价写法；
 *   - P6 起感知原语（self/closest/dist-to/name-of/direction-of）与比较原语
 *     （eq/lt/gt/le/ge）已实现并进 OPS，if/then/else 已实现为条件分支；
 *   - 方向常量 north/east/south/west 为保留 token（原子），move 支持 (move north)
 *     一元方向移动，与 (move dx dy) 统一为 MoveEffect(deltaX, deltaY)；
 *   - observe 尚未实现（P6.1 计划），写入文件会得到"未定义的顶层名称"的明确错误；
 *   - plan 直接内联引用运行时注入的自由变量 dx/dy（由 LLM Oracle 决定），
 *     不要写成 λdx. λdy. 形式。
 */

prog     : topLevel (SEMI? topLevel)* SEMI? EOF ;   // def 用 ';' 或换行分隔（换行被 WS 跳过，故分隔符可省略）

topLevel : defDecl
         | planDecl
         ;

defDecl  : NAME EQ expr ;
planDecl : PLAN EQ expr ;

expr     : lambda
         | let
         | ifExpr
         | seq
         | application
         | atom
         | LPAREN expr RPAREN        // 括号分组（嵌套 if/let/lambda 等）
         ;

lambda      : LAMBDA NAME DOT expr ;
let         : LET NAME EQ expr IN expr ;
ifExpr      : IF expr THEN expr ELSE expr ;
application : LPAREN callArg callArg* RPAREN   // 括号应用 (f a b)，零参 (f)
            | NAME callArg+                    // 并列应用 f a b（无括号，最自然写法）
            ;
callArg     : atom                             // 参数：原子（INT/STRING/NAME/方向）
            | LPAREN expr RPAREN               // 或括号表达式（嵌套应用/if 等）
            ;
seq         : LBRACE expr (SEMI expr)* RBRACE ;
atom        : INT | STRING | NAME | direction ;
direction   : NORTH | EAST | SOUTH | WEST ;

PLAN   : 'plan' ;
LAMBDA : 'lambda' | 'λ' ;
LET    : 'let' ;
IN     : 'in' ;
IF     : 'if' ;
THEN   : 'then' ;
ELSE   : 'else' ;
NORTH  : 'north' ;
EAST   : 'east' ;
SOUTH  : 'south' ;
WEST   : 'west' ;
DOT    : '.' ;
EQ     : '=' ;
LPAREN : '(' ;
RPAREN : ')' ;
LBRACE : '{' ;
RBRACE : '}' ;
SEMI   : ';' ;

INT    : '-'? [0-9]+ ;
STRING : '"' ( ~["\\\r\n] | '\\' . )* '"' ;
NAME   : [a-zA-Z_][a-zA-Z0-9_-]* ;

WS            : [ \t\r\n]+ -> skip ;
LINE_COMMENT  : ('//' | '#') ~[\r\n]* -> skip ;
BLOCK_COMMENT : '/*' .*? '*/' -> skip ;
