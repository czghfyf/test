/* Generated By:JJTree: Do not edit this line. ASTauxiliary_MDDL_NQ_statement.java Version 6.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=true,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package cn.bgotech.wormhole.olap.mdx.parser;

public
class ASTauxiliary_MDDL_NQ_statement extends SimpleNode {
  public ASTauxiliary_MDDL_NQ_statement(int id) {
    super(id);
  }

  public ASTauxiliary_MDDL_NQ_statement(WormholeMDXParser p, int id) {
    super(p, id);
  }


  /** Accept the visitor. **/
  public Object jjtAccept(WormholeMDXParserVisitor visitor, Object data) {

    return
    visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=5282fc68243eedb2d6b2da806b70ee6a (do not edit this line) */