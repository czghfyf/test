/* Generated By:JJTree: Do not edit this line. ASTmember_functions.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package cn.bgotech.wormhole.olap.mdx.parser;

public
class ASTmember_functions extends SimpleNode {
  public ASTmember_functions(int id) {
    super(id);
  }

  public ASTmember_functions(WormholeMDXParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WormholeMDXParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=588684befe692bc4d44add3347d7334c (do not edit this line) */
