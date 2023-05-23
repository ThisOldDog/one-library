grammar markdown;

md                : paragraph+
                  ;

paragraph         : text
                  | number
                  | header
                  | italic
                  | bold
                  | bold_italic
                  | strikethrough
                  | block_quote
                  | ordered_list
                  | unordered_list
                  | task_list
                  | code_inline
                  | dividing_line
                  | link
                  | link_url
                  | image
                  | tag
                  | table
                  | WS
                  ;

inline_paragraph  : text
                  | number
                  | header
                  | italic
                  | bold
                  | bold_italic
                  | strikethrough
                  | block_quote
                  | ordered_list
                  | unordered_list
                  | task_list
                  | code_inline
                  | dividing_line
                  | link
                  | link_url
                  | image
                  | tag
                  | table
                  | code_block
                  | multi_line_text
                  ;

text              : TEXT+
                  ;

number            : NUMBER+
                  ;


header            : '###### 'text*
                  | '##### 'text*
                  | '#### 'text*
                  | '### 'text*
                  | '## 'text*
                  | '# 'text*
                  ;

italic            : '*'inline_paragraph'*'
                  | '_'inline_paragraph'_'
                  ;

bold              : '**'inline_paragraph'**'
                  | '__'inline_paragraph'__'
                  ;

bold_italic       : '***'inline_paragraph'***'
                  | '___'inline_paragraph'___'
                  | '__*'inline_paragraph'*__'
                  ;

strikethrough     : '~~'inline_paragraph'~~'
                  ;

block_quote       : '>'+text*
                  ;

ordered_list      : NUMBER'. 'text*
                  ;

task_list         : '- [ ]'text*
                  | '- []'text*
                  | '- [x]'text*
                  | '- [X]'text*
                  ;

unordered_list    : '- 'text*
                  | '+ 'text*
                  | '* 'text*
                  ;

code_inline       : '`'text+'`'
                  ;

dividing_line     : '*'{3,}
                  | '-'{3,}
                  | '_'{3,}
                  ;

link              : '['text*']('text*')'
                  ;

link_url          : '['text*']['text*']'
                  | '['text*']: 'text*
                  ;

image             : '!['text*']('text*')'
                  | '[!['text*']('text*')]('text*')'
                  ;

tag               : TAG
                  ;

table             : '|'text+'|'
                  ;


multi_line_text   : line_text*
                  ;

line_text         : WS
                  | TEXT
                  ;

code_block        : '```'line_text*'```'WS
                  ;


TAG               : '<'[a-zA-Z0-9/]+'>'
                  ;

NUMBER            : [0-9]+
                  ;

TEXT              : [^\f\r\n]+
                  ;

WS                : [ \f\r\n\t]+
                  ;