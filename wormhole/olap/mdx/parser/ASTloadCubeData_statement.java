/* Generated By:JJTree: Do not edit this line. ASTloadCubeData_statement.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package cn.bgotech.wormhole.olap.mdx.parser;

public
class ASTloadCubeData_statement extends SimpleNode {
  public ASTloadCubeData_statement(int id) {
    super(id);
  }

  public ASTloadCubeData_statement(WormholeMDXParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WormholeMDXParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=956476d1a41b23da5916bd87c456a72c (do not edit this line) */
