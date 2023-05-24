grammar JSON;

json: value;

value: objectValue
     | arrayValue
     | STRING
     | NUMBER
     | 'true'
     | 'false'
     | 'null';

objectValue: '{' (pair (',' pair)*)? '}';

pair: STRING ':' value;

arrayValue: '[' (value (',' value)*)? ']';

STRING: '"' (~["\\] | ESCAPE)* '"';
NUMBER: '-'? INT ('.' [0-9]+)? EXP?;
fragment INT: '0' | [1-9] [0-9]*;
fragment EXP: [Ee] [+\-]? INT;
fragment ESCAPE: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];

WS: [ \t\r\n]+ -> skip;
