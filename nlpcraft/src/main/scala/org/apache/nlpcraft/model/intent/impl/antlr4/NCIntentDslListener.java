// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4/NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCIntentDslParser}.
 */
public interface NCIntentDslListener extends ParseTreeListener {
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#clsNer}.
	 * @param ctx the parse tree
	 */
	void enterClsNer(NCIntentDslParser.ClsNerContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#clsNer}.
	 * @param ctx the parse tree
	 */
	void exitClsNer(NCIntentDslParser.ClsNerContext ctx);
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(NCIntentDslParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(NCIntentDslParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void enterUnaryExpr(NCIntentDslParser.UnaryExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#unaryExpr}.
	 * @param ctx the parse tree
	 */
	void exitUnaryExpr(NCIntentDslParser.UnaryExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#funCall}.
	 * @param ctx the parse tree
	 */
	void enterFunCall(NCIntentDslParser.FunCallContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#funCall}.
	 * @param ctx the parse tree
	 */
	void exitFunCall(NCIntentDslParser.FunCallContext ctx);
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
}