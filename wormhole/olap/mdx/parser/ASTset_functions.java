/* Generated By:JJTree: Do not edit this line. ASTset_functions.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package cn.bgotech.wormhole.olap.mdx.parser;

public
class ASTset_functions extends SimpleNode {
  public ASTset_functions(int id) {
    super(id);
  }

  public ASTset_functions(WormholeMDXParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WormholeMDXParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=ce22e6b5f3f6baa8a22ffe74bb62ebe5 (do not edit this line) */