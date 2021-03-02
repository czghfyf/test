/* Generated By:JavaCC: Do not edit this line. WormholeMDXParserDefaultVisitor.java Version 6.0_1 */
package cn.bgotech.wormhole.olap.mdx.parser;

public class WormholeMDXParserDefaultVisitor implements WormholeMDXParserVisitor{
  public Object defaultVisit(SimpleNode node, Object data){
    node.childrenAccept(this, data);
    return data;
  }
  public Object visit(SimpleNode node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTexecute node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTauxiliary_MDDL_NQ_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTcreate_dimension_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTcreate_member_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbuild_cube_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTsync_cube_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTwriteback_cube_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTint_upt_del_measure_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTCUD_measure_stat_V node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTload_cube_data_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTcreate_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTloadCubeData_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTbuild_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTinsert_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTupdate_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTdelete_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTselect_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTwith_statement_def node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTset_formula_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTmember_formula_statement node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTexpression node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTterm node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTfactory node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTcommon_functions node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTset node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTset_functions node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTdescendants_options node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTboolean_expression node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTboolean_term node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTboolean_factory node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTboolean_function node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTlevel node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTtuple node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTmember node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTmember_functions node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTdimension node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTaxis_statement_def node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTaxis_alias node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTwhere_statement_def node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTcube node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTmulti_dimensional_domain_select node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTdomain_selector_part node, Object data){
    return defaultVisit(node, data);
  }
  public Object visit(ASTsigned_double_statement node, Object data){
    return defaultVisit(node, data);
  }
}
/* JavaCC - OriginalChecksum=c03122bba7196cd1418389dccb2f8938 (do not edit this line) */