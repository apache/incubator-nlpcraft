// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/internal/intent/compiler/antlr4\NCIDL.g4 by ANTLR 4.9.2
package org.apache.nlpcraft.internal.intent.compiler.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCIDLParser}.
 */
public interface NCIDLListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#idl}.
	 * @param ctx the parse tree
	 */
	void enterIdl(NCIDLParser.IdlContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#idl}.
	 * @param ctx the parse tree
	 */
	void exitIdl(NCIDLParser.IdlContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#idlDecls}.
	 * @param ctx the parse tree
	 */
	void enterIdlDecls(NCIDLParser.IdlDeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#idlDecls}.
	 * @param ctx the parse tree
	 */
	void exitIdlDecls(NCIDLParser.IdlDeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#idlDecl}.
	 * @param ctx the parse tree
	 */
	void enterIdlDecl(NCIDLParser.IdlDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#idlDecl}.
	 * @param ctx the parse tree
	 */
	void exitIdlDecl(NCIDLParser.IdlDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#imprt}.
	 * @param ctx the parse tree
	 */
	void enterImprt(NCIDLParser.ImprtContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#imprt}.
	 * @param ctx the parse tree
	 */
	void exitImprt(NCIDLParser.ImprtContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#frag}.
	 * @param ctx the parse tree
	 */
	void enterFrag(NCIDLParser.FragContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#frag}.
	 * @param ctx the parse tree
	 */
	void exitFrag(NCIDLParser.FragContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#fragId}.
	 * @param ctx the parse tree
	 */
	void enterFragId(NCIDLParser.FragIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#fragId}.
	 * @param ctx the parse tree
	 */
	void exitFragId(NCIDLParser.FragIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void enterFragRef(NCIDLParser.FragRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void exitFragRef(NCIDLParser.FragRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void enterFragMeta(NCIDLParser.FragMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void exitFragMeta(NCIDLParser.FragMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#intent}.
	 * @param ctx the parse tree
	 */
	void enterIntent(NCIDLParser.IntentContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#intent}.
	 * @param ctx the parse tree
	 */
	void exitIntent(NCIDLParser.IntentContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#intentId}.
	 * @param ctx the parse tree
	 */
	void enterIntentId(NCIDLParser.IntentIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#intentId}.
	 * @param ctx the parse tree
	 */
	void exitIntentId(NCIDLParser.IntentIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void enterFlowDecl(NCIDLParser.FlowDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void exitFlowDecl(NCIDLParser.FlowDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void enterMetaDecl(NCIDLParser.MetaDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void exitMetaDecl(NCIDLParser.MetaDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#optDecl}.
	 * @param ctx the parse tree
	 */
	void enterOptDecl(NCIDLParser.OptDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#optDecl}.
	 * @param ctx the parse tree
	 */
	void exitOptDecl(NCIDLParser.OptDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void enterJsonObj(NCIDLParser.JsonObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void exitJsonObj(NCIDLParser.JsonObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void enterJsonPair(NCIDLParser.JsonPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void exitJsonPair(NCIDLParser.JsonPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void enterJsonVal(NCIDLParser.JsonValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void exitJsonVal(NCIDLParser.JsonValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void enterJsonArr(NCIDLParser.JsonArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void exitJsonArr(NCIDLParser.JsonArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#termDecls}.
	 * @param ctx the parse tree
	 */
	void enterTermDecls(NCIDLParser.TermDeclsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#termDecls}.
	 * @param ctx the parse tree
	 */
	void exitTermDecls(NCIDLParser.TermDeclsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#termDecl}.
	 * @param ctx the parse tree
	 */
	void enterTermDecl(NCIDLParser.TermDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#termDecl}.
	 * @param ctx the parse tree
	 */
	void exitTermDecl(NCIDLParser.TermDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#termEq}.
	 * @param ctx the parse tree
	 */
	void enterTermEq(NCIDLParser.TermEqContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#termEq}.
	 * @param ctx the parse tree
	 */
	void exitTermEq(NCIDLParser.TermEqContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(NCIDLParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(NCIDLParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void enterMtdRef(NCIDLParser.MtdRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void exitMtdRef(NCIDLParser.MtdRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void enterJavaFqn(NCIDLParser.JavaFqnContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void exitJavaFqn(NCIDLParser.JavaFqnContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#javaClass}.
	 * @param ctx the parse tree
	 */
	void enterJavaClass(NCIDLParser.JavaClassContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#javaClass}.
	 * @param ctx the parse tree
	 */
	void exitJavaClass(NCIDLParser.JavaClassContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#termId}.
	 * @param ctx the parse tree
	 */
	void enterTermId(NCIDLParser.TermIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#termId}.
	 * @param ctx the parse tree
	 */
	void exitTermId(NCIDLParser.TermIdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParExpr(NCIDLParser.ParExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParExpr(NCIDLParser.ParExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(NCIDLParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(NCIDLParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCompExpr(NCIDLParser.CompExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCompExpr(NCIDLParser.CompExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusMinusExpr(NCIDLParser.PlusMinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusMinusExpr(NCIDLParser.PlusMinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(NCIDLParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(NCIDLParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code varRef}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVarRef(NCIDLParser.VarRefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code varRef}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVarRef(NCIDLParser.VarRefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultDivModExpr(NCIDLParser.MultDivModExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultDivModExpr(NCIDLParser.MultDivModExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndOrExpr(NCIDLParser.AndOrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndOrExpr(NCIDLParser.AndOrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCallExpr(NCIDLParser.CallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCallExpr(NCIDLParser.CallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqNeqExpr(NCIDLParser.EqNeqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIDLParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqNeqExpr(NCIDLParser.EqNeqExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#vars}.
	 * @param ctx the parse tree
	 */
	void enterVars(NCIDLParser.VarsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#vars}.
	 * @param ctx the parse tree
	 */
	void exitVars(NCIDLParser.VarsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void enterVarDecl(NCIDLParser.VarDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#varDecl}.
	 * @param ctx the parse tree
	 */
	void exitVarDecl(NCIDLParser.VarDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(NCIDLParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(NCIDLParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(NCIDLParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(NCIDLParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#qstring}.
	 * @param ctx the parse tree
	 */
	void enterQstring(NCIDLParser.QstringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#qstring}.
	 * @param ctx the parse tree
	 */
	void exitQstring(NCIDLParser.QstringContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#minMax}.
	 * @param ctx the parse tree
	 */
	void enterMinMax(NCIDLParser.MinMaxContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#minMax}.
	 * @param ctx the parse tree
	 */
	void exitMinMax(NCIDLParser.MinMaxContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxShortcut(NCIDLParser.MinMaxShortcutContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxShortcut(NCIDLParser.MinMaxShortcutContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxRange(NCIDLParser.MinMaxRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxRange(NCIDLParser.MinMaxRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIDLParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(NCIDLParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIDLParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(NCIDLParser.IdContext ctx);
}