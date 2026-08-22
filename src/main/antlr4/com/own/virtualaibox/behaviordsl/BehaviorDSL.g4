grammar BehaviorDSL;

/*
 * P5 行为 DSL（ANTLR 4.13）。对应 docs/secd-fusion-design.md §10，语法 v0.1：
 *
 *   program   := topLevel (';' topLevel)* ';'? EOF
 *   topLevel  := NAME '=' expr        # def：命名行为（onMeet 等 λ 行为）
 *              | 'plan' '=' expr      # plan：入口程序（内联，直接用 dx/dy）
 *   expr      := INT | STRING | NAME
 *              | expr expr+             # 并列应用 f a b（柯里化左结合）
 *              | '(' expr expr+ ')'     # 带括号应用 (f a b)，等价形式
 *              | ('lambda'|'λ') NAME '.' expr
 *              | 'let' NAME '=' expr 'in' expr
 *              | 'if' expr 'then' expr 'else' expr   # 保留字，尚未实现
 *              | '{' expr (';' expr)* '}'
 *
 * 说明（相对 §10 草案的收敛）：
 *   - move 在运行时是柯里化二元原语，故应用改为变参形式 f a b == ((f a) b)，
 *     括号 (f a b) 为等价写法；
 *   - observe / name-of / dist-to / 方向常量（north/east/…）尚未在 WorldOpEvaluator
 *     实现，故不进文法，写入文件会得到"未定义的顶层名称"的明确错误；
 *   - plan 直接内联引用运行时注入的自由变量 dx/dy（由 LLM Oracle 决定），
 *     不要写成 λdx. λdy. 形式。
 */

prog     : topLevel (SEMI topLevel)* SEMI? EOF ;

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
         ;

lambda      : LAMBDA NAME DOT expr ;
let         : LET NAME EQ expr IN expr ;
ifExpr      : IF expr THEN expr ELSE expr ;
application : LPAREN expr expr+ RPAREN   # 带括号变参应用 (f a b)
            | NAME atom+                  # 并列应用 f a b（无括号，最自然写法；参数须为原子，
                                         #   避免 expr 贪婪吞掉后续参数导致结合方向错误）
            ;
seq         : LBRACE expr (SEMI expr)* RBRACE ;
atom        : INT | STRING | NAME ;

PLAN   : 'plan' ;
LAMBDA : 'lambda' | 'λ' ;
LET    : 'let' ;
IN     : 'in' ;
IF     : 'if' ;
THEN   : 'then' ;
ELSE   : 'else' ;
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
