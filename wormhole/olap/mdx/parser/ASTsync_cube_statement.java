/* Generated By:JJTree: Do not edit this line. ASTsync_cube_statement.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package cn.bgotech.wormhole.olap.mdx.parser;

public
class ASTsync_cube_statement extends SimpleNode {
  public ASTsync_cube_statement(int id) {
    super(id);
  }

  public ASTsync_cube_statement(WormholeMDXParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WormholeMDXParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=fdf7099765021c57841e219d9ed04869 (do not edit this line) */