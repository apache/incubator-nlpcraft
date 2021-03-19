// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/compiler/antlr4\NCIdl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCIdlParser}.
 */
public interface NCIdlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#idl}.
	 * @param ctx the parse tree
	 */
	void enterIdl(NCIdlParser.IdlContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#idl}.
	 * @param ctx the parse tree
	 */
	void exitIdl(NCIdlParser.IdlContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#synonym}.
	 * @param ctx the parse tree
	 */
	void enterSynonym(NCIdlParser.SynonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#synonym}.
	 * @param ctx the parse tree
	 */
	void exitSynonym(NCIdlParser.SynonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(NCIdlParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(NCIdlParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#idlItems}.
	 * @param ctx the parse tree
	 */
	void enterIdlItems(NCIdlParser.IdlItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#idlItems}.
	 * @param ctx the parse tree
	 */
	void exitIdlItems(NCIdlParser.IdlItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#idlItem}.
	 * @param ctx the parse tree
	 */
	void enterIdlItem(NCIdlParser.IdlItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#idlItem}.
	 * @param ctx the parse tree
	 */
	void exitIdlItem(NCIdlParser.IdlItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#imp}.
	 * @param ctx the parse tree
	 */
	void enterImp(NCIdlParser.ImpContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#imp}.
	 * @param ctx the parse tree
	 */
	void exitImp(NCIdlParser.ImpContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#frag}.
	 * @param ctx the parse tree
	 */
	void enterFrag(NCIdlParser.FragContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#frag}.
	 * @param ctx the parse tree
	 */
	void exitFrag(NCIdlParser.FragContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#fragId}.
	 * @param ctx the parse tree
	 */
	void enterFragId(NCIdlParser.FragIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#fragId}.
	 * @param ctx the parse tree
	 */
	void exitFragId(NCIdlParser.FragIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void enterFragRef(NCIdlParser.FragRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void exitFragRef(NCIdlParser.FragRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void enterFragMeta(NCIdlParser.FragMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void exitFragMeta(NCIdlParser.FragMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#intent}.
	 * @param ctx the parse tree
	 */
	void enterIntent(NCIdlParser.IntentContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#intent}.
	 * @param ctx the parse tree
	 */
	void exitIntent(NCIdlParser.IntentContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#intentId}.
	 * @param ctx the parse tree
	 */
	void enterIntentId(NCIdlParser.IntentIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#intentId}.
	 * @param ctx the parse tree
	 */
	void exitIntentId(NCIdlParser.IntentIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#orderedDecl}.
	 * @param ctx the parse tree
	 */
	void enterOrderedDecl(NCIdlParser.OrderedDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#orderedDecl}.
	 * @param ctx the parse tree
	 */
	void exitOrderedDecl(NCIdlParser.OrderedDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#mtdDecl}.
	 * @param ctx the parse tree
	 */
	void enterMtdDecl(NCIdlParser.MtdDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#mtdDecl}.
	 * @param ctx the parse tree
	 */
	void exitMtdDecl(NCIdlParser.MtdDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void enterFlowDecl(NCIdlParser.FlowDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void exitFlowDecl(NCIdlParser.FlowDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void enterMetaDecl(NCIdlParser.MetaDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void exitMetaDecl(NCIdlParser.MetaDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void enterJsonObj(NCIdlParser.JsonObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void exitJsonObj(NCIdlParser.JsonObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void enterJsonPair(NCIdlParser.JsonPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void exitJsonPair(NCIdlParser.JsonPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void enterJsonVal(NCIdlParser.JsonValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void exitJsonVal(NCIdlParser.JsonValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void enterJsonArr(NCIdlParser.JsonArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void exitJsonArr(NCIdlParser.JsonArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#terms}.
	 * @param ctx the parse tree
	 */
	void enterTerms(NCIdlParser.TermsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#terms}.
	 * @param ctx the parse tree
	 */
	void exitTerms(NCIdlParser.TermsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#termItem}.
	 * @param ctx the parse tree
	 */
	void enterTermItem(NCIdlParser.TermItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#termItem}.
	 * @param ctx the parse tree
	 */
	void exitTermItem(NCIdlParser.TermItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#termEq}.
	 * @param ctx the parse tree
	 */
	void enterTermEq(NCIdlParser.TermEqContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#termEq}.
	 * @param ctx the parse tree
	 */
	void exitTermEq(NCIdlParser.TermEqContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(NCIdlParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(NCIdlParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void enterMtdRef(NCIdlParser.MtdRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void exitMtdRef(NCIdlParser.MtdRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void enterJavaFqn(NCIdlParser.JavaFqnContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void exitJavaFqn(NCIdlParser.JavaFqnContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#termId}.
	 * @param ctx the parse tree
	 */
	void enterTermId(NCIdlParser.TermIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#termId}.
	 * @param ctx the parse tree
	 */
	void exitTermId(NCIdlParser.TermIdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParExpr(NCIdlParser.ParExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParExpr(NCIdlParser.ParExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(NCIdlParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(NCIdlParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCompExpr(NCIdlParser.CompExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCompExpr(NCIdlParser.CompExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusMinusExpr(NCIdlParser.PlusMinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusMinusExpr(NCIdlParser.PlusMinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(NCIdlParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(NCIdlParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultDivModExpr(NCIdlParser.MultDivModExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultDivModExpr(NCIdlParser.MultDivModExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndOrExpr(NCIdlParser.AndOrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndOrExpr(NCIdlParser.AndOrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCallExpr(NCIdlParser.CallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCallExpr(NCIdlParser.CallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqNeqExpr(NCIdlParser.EqNeqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqNeqExpr(NCIdlParser.EqNeqExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(NCIdlParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(NCIdlParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(NCIdlParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(NCIdlParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#qstring}.
	 * @param ctx the parse tree
	 */
	void enterQstring(NCIdlParser.QstringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#qstring}.
	 * @param ctx the parse tree
	 */
	void exitQstring(NCIdlParser.QstringContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#minMax}.
	 * @param ctx the parse tree
	 */
	void enterMinMax(NCIdlParser.MinMaxContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#minMax}.
	 * @param ctx the parse tree
	 */
	void exitMinMax(NCIdlParser.MinMaxContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxShortcut(NCIdlParser.MinMaxShortcutContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxShortcut(NCIdlParser.MinMaxShortcutContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxRange(NCIdlParser.MinMaxRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxRange(NCIdlParser.MinMaxRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIdlParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(NCIdlParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIdlParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(NCIdlParser.IdContext ctx);
}