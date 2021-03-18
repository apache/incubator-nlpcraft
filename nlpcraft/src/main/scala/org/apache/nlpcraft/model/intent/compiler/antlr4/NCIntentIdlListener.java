// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/compiler/antlr4\NCIntentIdl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCIntentIdlParser}.
 */
public interface NCIntentIdlListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#idl}.
	 * @param ctx the parse tree
	 */
	void enterIdl(NCIntentIdlParser.IdlContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#idl}.
	 * @param ctx the parse tree
	 */
	void exitIdl(NCIntentIdlParser.IdlContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#synonym}.
	 * @param ctx the parse tree
	 */
	void enterSynonym(NCIntentIdlParser.SynonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#synonym}.
	 * @param ctx the parse tree
	 */
	void exitSynonym(NCIntentIdlParser.SynonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(NCIntentIdlParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(NCIntentIdlParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#idlItems}.
	 * @param ctx the parse tree
	 */
	void enterIdlItems(NCIntentIdlParser.IdlItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#idlItems}.
	 * @param ctx the parse tree
	 */
	void exitIdlItems(NCIntentIdlParser.IdlItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#idlItem}.
	 * @param ctx the parse tree
	 */
	void enterIdlItem(NCIntentIdlParser.IdlItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#idlItem}.
	 * @param ctx the parse tree
	 */
	void exitIdlItem(NCIntentIdlParser.IdlItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#imp}.
	 * @param ctx the parse tree
	 */
	void enterImp(NCIntentIdlParser.ImpContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#imp}.
	 * @param ctx the parse tree
	 */
	void exitImp(NCIntentIdlParser.ImpContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#frag}.
	 * @param ctx the parse tree
	 */
	void enterFrag(NCIntentIdlParser.FragContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#frag}.
	 * @param ctx the parse tree
	 */
	void exitFrag(NCIntentIdlParser.FragContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#fragId}.
	 * @param ctx the parse tree
	 */
	void enterFragId(NCIntentIdlParser.FragIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#fragId}.
	 * @param ctx the parse tree
	 */
	void exitFragId(NCIntentIdlParser.FragIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void enterFragRef(NCIntentIdlParser.FragRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void exitFragRef(NCIntentIdlParser.FragRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void enterFragMeta(NCIntentIdlParser.FragMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void exitFragMeta(NCIntentIdlParser.FragMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#intent}.
	 * @param ctx the parse tree
	 */
	void enterIntent(NCIntentIdlParser.IntentContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#intent}.
	 * @param ctx the parse tree
	 */
	void exitIntent(NCIntentIdlParser.IntentContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#intentId}.
	 * @param ctx the parse tree
	 */
	void enterIntentId(NCIntentIdlParser.IntentIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#intentId}.
	 * @param ctx the parse tree
	 */
	void exitIntentId(NCIntentIdlParser.IntentIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#orderedDecl}.
	 * @param ctx the parse tree
	 */
	void enterOrderedDecl(NCIntentIdlParser.OrderedDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#orderedDecl}.
	 * @param ctx the parse tree
	 */
	void exitOrderedDecl(NCIntentIdlParser.OrderedDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#mtdDecl}.
	 * @param ctx the parse tree
	 */
	void enterMtdDecl(NCIntentIdlParser.MtdDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#mtdDecl}.
	 * @param ctx the parse tree
	 */
	void exitMtdDecl(NCIntentIdlParser.MtdDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void enterFlowDecl(NCIntentIdlParser.FlowDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void exitFlowDecl(NCIntentIdlParser.FlowDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void enterMetaDecl(NCIntentIdlParser.MetaDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void exitMetaDecl(NCIntentIdlParser.MetaDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void enterJsonObj(NCIntentIdlParser.JsonObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void exitJsonObj(NCIntentIdlParser.JsonObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void enterJsonPair(NCIntentIdlParser.JsonPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void exitJsonPair(NCIntentIdlParser.JsonPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void enterJsonVal(NCIntentIdlParser.JsonValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void exitJsonVal(NCIntentIdlParser.JsonValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void enterJsonArr(NCIntentIdlParser.JsonArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void exitJsonArr(NCIntentIdlParser.JsonArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#terms}.
	 * @param ctx the parse tree
	 */
	void enterTerms(NCIntentIdlParser.TermsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#terms}.
	 * @param ctx the parse tree
	 */
	void exitTerms(NCIntentIdlParser.TermsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#termItem}.
	 * @param ctx the parse tree
	 */
	void enterTermItem(NCIntentIdlParser.TermItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#termItem}.
	 * @param ctx the parse tree
	 */
	void exitTermItem(NCIntentIdlParser.TermItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#termEq}.
	 * @param ctx the parse tree
	 */
	void enterTermEq(NCIntentIdlParser.TermEqContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#termEq}.
	 * @param ctx the parse tree
	 */
	void exitTermEq(NCIntentIdlParser.TermEqContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(NCIntentIdlParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(NCIntentIdlParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void enterMtdRef(NCIntentIdlParser.MtdRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void exitMtdRef(NCIntentIdlParser.MtdRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void enterJavaFqn(NCIntentIdlParser.JavaFqnContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void exitJavaFqn(NCIntentIdlParser.JavaFqnContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#termId}.
	 * @param ctx the parse tree
	 */
	void enterTermId(NCIntentIdlParser.TermIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#termId}.
	 * @param ctx the parse tree
	 */
	void exitTermId(NCIntentIdlParser.TermIdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParExpr(NCIntentIdlParser.ParExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParExpr(NCIntentIdlParser.ParExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(NCIntentIdlParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(NCIntentIdlParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCompExpr(NCIntentIdlParser.CompExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCompExpr(NCIntentIdlParser.CompExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusMinusExpr(NCIntentIdlParser.PlusMinusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusMinusExpr(NCIntentIdlParser.PlusMinusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(NCIntentIdlParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(NCIntentIdlParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultDivModExpr(NCIntentIdlParser.MultDivModExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultDivModExpr(NCIntentIdlParser.MultDivModExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAndOrExpr(NCIntentIdlParser.AndOrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAndOrExpr(NCIntentIdlParser.AndOrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCallExpr(NCIntentIdlParser.CallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCallExpr(NCIntentIdlParser.CallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqNeqExpr(NCIntentIdlParser.EqNeqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqNeqExpr(NCIntentIdlParser.EqNeqExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(NCIntentIdlParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(NCIntentIdlParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(NCIntentIdlParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(NCIntentIdlParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#qstring}.
	 * @param ctx the parse tree
	 */
	void enterQstring(NCIntentIdlParser.QstringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#qstring}.
	 * @param ctx the parse tree
	 */
	void exitQstring(NCIntentIdlParser.QstringContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#minMax}.
	 * @param ctx the parse tree
	 */
	void enterMinMax(NCIntentIdlParser.MinMaxContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#minMax}.
	 * @param ctx the parse tree
	 */
	void exitMinMax(NCIntentIdlParser.MinMaxContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxShortcut(NCIntentIdlParser.MinMaxShortcutContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxShortcut(NCIntentIdlParser.MinMaxShortcutContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxRange(NCIntentIdlParser.MinMaxRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxRange(NCIntentIdlParser.MinMaxRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentIdlParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(NCIntentIdlParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentIdlParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(NCIntentIdlParser.IdContext ctx);
}