/* Generated By:JJTree: Do not edit this line. ASTdimension.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package cn.bgotech.wormhole.olap.mdx.parser;

public
class ASTdimension extends SimpleNode {
  public ASTdimension(int id) {
    super(id);
  }

  public ASTdimension(WormholeMDXParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WormholeMDXParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=bf5297bbb2d8ba6b23a415abd674ada1 (do not edit this line) */
