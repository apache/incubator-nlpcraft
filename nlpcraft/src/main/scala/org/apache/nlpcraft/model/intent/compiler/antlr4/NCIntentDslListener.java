// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCIntentDslParser}.
 */
public interface NCIntentDslListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#dsl}.
	 * @param ctx the parse tree
	 */
	void enterDsl(NCIntentDslParser.DslContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#dsl}.
	 * @param ctx the parse tree
	 */
	void exitDsl(NCIntentDslParser.DslContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#synonym}.
	 * @param ctx the parse tree
	 */
	void enterSynonym(NCIntentDslParser.SynonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#synonym}.
	 * @param ctx the parse tree
	 */
	void exitSynonym(NCIntentDslParser.SynonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(NCIntentDslParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(NCIntentDslParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#dslItems}.
	 * @param ctx the parse tree
	 */
	void enterDslItems(NCIntentDslParser.DslItemsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#dslItems}.
	 * @param ctx the parse tree
	 */
	void exitDslItems(NCIntentDslParser.DslItemsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#dslItem}.
	 * @param ctx the parse tree
	 */
	void enterDslItem(NCIntentDslParser.DslItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#dslItem}.
	 * @param ctx the parse tree
	 */
	void exitDslItem(NCIntentDslParser.DslItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#frag}.
	 * @param ctx the parse tree
	 */
	void enterFrag(NCIntentDslParser.FragContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#frag}.
	 * @param ctx the parse tree
	 */
	void exitFrag(NCIntentDslParser.FragContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#fragId}.
	 * @param ctx the parse tree
	 */
	void enterFragId(NCIntentDslParser.FragIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#fragId}.
	 * @param ctx the parse tree
	 */
	void exitFragId(NCIntentDslParser.FragIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void enterFragRef(NCIntentDslParser.FragRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#fragRef}.
	 * @param ctx the parse tree
	 */
	void exitFragRef(NCIntentDslParser.FragRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void enterFragMeta(NCIntentDslParser.FragMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#fragMeta}.
	 * @param ctx the parse tree
	 */
	void exitFragMeta(NCIntentDslParser.FragMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#intent}.
	 * @param ctx the parse tree
	 */
	void enterIntent(NCIntentDslParser.IntentContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#intent}.
	 * @param ctx the parse tree
	 */
	void exitIntent(NCIntentDslParser.IntentContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#intentId}.
	 * @param ctx the parse tree
	 */
	void enterIntentId(NCIntentDslParser.IntentIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#intentId}.
	 * @param ctx the parse tree
	 */
	void exitIntentId(NCIntentDslParser.IntentIdContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#orderedDecl}.
	 * @param ctx the parse tree
	 */
	void enterOrderedDecl(NCIntentDslParser.OrderedDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#orderedDecl}.
	 * @param ctx the parse tree
	 */
	void exitOrderedDecl(NCIntentDslParser.OrderedDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#mtdDecl}.
	 * @param ctx the parse tree
	 */
	void enterMtdDecl(NCIntentDslParser.MtdDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#mtdDecl}.
	 * @param ctx the parse tree
	 */
	void exitMtdDecl(NCIntentDslParser.MtdDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void enterFlowDecl(NCIntentDslParser.FlowDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#flowDecl}.
	 * @param ctx the parse tree
	 */
	void exitFlowDecl(NCIntentDslParser.FlowDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void enterMetaDecl(NCIntentDslParser.MetaDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#metaDecl}.
	 * @param ctx the parse tree
	 */
	void exitMetaDecl(NCIntentDslParser.MetaDeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void enterJsonObj(NCIntentDslParser.JsonObjContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#jsonObj}.
	 * @param ctx the parse tree
	 */
	void exitJsonObj(NCIntentDslParser.JsonObjContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void enterJsonPair(NCIntentDslParser.JsonPairContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#jsonPair}.
	 * @param ctx the parse tree
	 */
	void exitJsonPair(NCIntentDslParser.JsonPairContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void enterJsonVal(NCIntentDslParser.JsonValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#jsonVal}.
	 * @param ctx the parse tree
	 */
	void exitJsonVal(NCIntentDslParser.JsonValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void enterJsonArr(NCIntentDslParser.JsonArrContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#jsonArr}.
	 * @param ctx the parse tree
	 */
	void exitJsonArr(NCIntentDslParser.JsonArrContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#terms}.
	 * @param ctx the parse tree
	 */
	void enterTerms(NCIntentDslParser.TermsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#terms}.
	 * @param ctx the parse tree
	 */
	void exitTerms(NCIntentDslParser.TermsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#termItem}.
	 * @param ctx the parse tree
	 */
	void enterTermItem(NCIntentDslParser.TermItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#termItem}.
	 * @param ctx the parse tree
	 */
	void exitTermItem(NCIntentDslParser.TermItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#termEq}.
	 * @param ctx the parse tree
	 */
	void enterTermEq(NCIntentDslParser.TermEqContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#termEq}.
	 * @param ctx the parse tree
	 */
	void exitTermEq(NCIntentDslParser.TermEqContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(NCIntentDslParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(NCIntentDslParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void enterMtdRef(NCIntentDslParser.MtdRefContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#mtdRef}.
	 * @param ctx the parse tree
	 */
	void exitMtdRef(NCIntentDslParser.MtdRefContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void enterJavaFqn(NCIntentDslParser.JavaFqnContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#javaFqn}.
	 * @param ctx the parse tree
	 */
	void exitJavaFqn(NCIntentDslParser.JavaFqnContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#termId}.
	 * @param ctx the parse tree
	 */
	void enterTermId(NCIntentDslParser.TermIdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#termId}.
	 * @param ctx the parse tree
	 */
	void exitTermId(NCIntentDslParser.TermIdContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParExpr(NCIntentDslParser.ParExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParExpr(NCIntentDslParser.ParExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eqExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqExpr(NCIntentDslParser.EqExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eqExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqExpr(NCIntentDslParser.EqExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(NCIntentDslParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(NCIntentDslParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCompExpr(NCIntentDslParser.CompExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCompExpr(NCIntentDslParser.CompExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAtomExpr(NCIntentDslParser.AtomExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAtomExpr(NCIntentDslParser.AtomExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterCallExpr(NCIntentDslParser.CallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitCallExpr(NCIntentDslParser.CallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultExpr(NCIntentDslParser.MultExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultExpr(NCIntentDslParser.MultExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code plusExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterPlusExpr(NCIntentDslParser.PlusExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code plusExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitPlusExpr(NCIntentDslParser.PlusExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLogExpr(NCIntentDslParser.LogExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLogExpr(NCIntentDslParser.LogExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#paramList}.
	 * @param ctx the parse tree
	 */
	void enterParamList(NCIntentDslParser.ParamListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#paramList}.
	 * @param ctx the parse tree
	 */
	void exitParamList(NCIntentDslParser.ParamListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#atom}.
	 * @param ctx the parse tree
	 */
	void enterAtom(NCIntentDslParser.AtomContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#atom}.
	 * @param ctx the parse tree
	 */
	void exitAtom(NCIntentDslParser.AtomContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#qstring}.
	 * @param ctx the parse tree
	 */
	void enterQstring(NCIntentDslParser.QstringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#qstring}.
	 * @param ctx the parse tree
	 */
	void exitQstring(NCIntentDslParser.QstringContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#minMax}.
	 * @param ctx the parse tree
	 */
	void enterMinMax(NCIntentDslParser.MinMaxContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#minMax}.
	 * @param ctx the parse tree
	 */
	void exitMinMax(NCIntentDslParser.MinMaxContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxShortcut(NCIntentDslParser.MinMaxShortcutContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxShortcut(NCIntentDslParser.MinMaxShortcutContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxRange(NCIntentDslParser.MinMaxRangeContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#minMaxRange}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxRange(NCIntentDslParser.MinMaxRangeContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#id}.
	 * @param ctx the parse tree
	 */
	void enterId(NCIntentDslParser.IdContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#id}.
	 * @param ctx the parse tree
	 */
	void exitId(NCIntentDslParser.IdContext ctx);
}