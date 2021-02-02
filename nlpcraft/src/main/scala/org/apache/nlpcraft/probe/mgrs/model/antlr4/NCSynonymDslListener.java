// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4\NCSynonymDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.probe.mgrs.model.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCSynonymDslParser}.
 */
public interface NCSynonymDslListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#synonym}.
	 * @param ctx the parse tree
	 */
	void enterSynonym(NCSynonymDslParser.SynonymContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#synonym}.
	 * @param ctx the parse tree
	 */
	void exitSynonym(NCSynonymDslParser.SynonymContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#alias}.
	 * @param ctx the parse tree
	 */
	void enterAlias(NCSynonymDslParser.AliasContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#alias}.
	 * @param ctx the parse tree
	 */
	void exitAlias(NCSynonymDslParser.AliasContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#item}.
	 * @param ctx the parse tree
	 */
	void enterItem(NCSynonymDslParser.ItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#item}.
	 * @param ctx the parse tree
	 */
	void exitItem(NCSynonymDslParser.ItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#pred}.
	 * @param ctx the parse tree
	 */
	void enterPred(NCSynonymDslParser.PredContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#pred}.
	 * @param ctx the parse tree
	 */
	void exitPred(NCSynonymDslParser.PredContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(NCSynonymDslParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(NCSynonymDslParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#mathOp}.
	 * @param ctx the parse tree
	 */
	void enterMathOp(NCSynonymDslParser.MathOpContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#mathOp}.
	 * @param ctx the parse tree
	 */
	void exitMathOp(NCSynonymDslParser.MathOpContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#val}.
	 * @param ctx the parse tree
	 */
	void enterVal(NCSynonymDslParser.ValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#val}.
	 * @param ctx the parse tree
	 */
	void exitVal(NCSynonymDslParser.ValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#singleVal}.
	 * @param ctx the parse tree
	 */
	void enterSingleVal(NCSynonymDslParser.SingleValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#singleVal}.
	 * @param ctx the parse tree
	 */
	void exitSingleVal(NCSynonymDslParser.SingleValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#tokQual}.
	 * @param ctx the parse tree
	 */
	void enterTokQual(NCSynonymDslParser.TokQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#tokQual}.
	 * @param ctx the parse tree
	 */
	void exitTokQual(NCSynonymDslParser.TokQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#tokQualPart}.
	 * @param ctx the parse tree
	 */
	void enterTokQualPart(NCSynonymDslParser.TokQualPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#tokQualPart}.
	 * @param ctx the parse tree
	 */
	void exitTokQualPart(NCSynonymDslParser.TokQualPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#tokMeta}.
	 * @param ctx the parse tree
	 */
	void enterTokMeta(NCSynonymDslParser.TokMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#tokMeta}.
	 * @param ctx the parse tree
	 */
	void exitTokMeta(NCSynonymDslParser.TokMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#modelMeta}.
	 * @param ctx the parse tree
	 */
	void enterModelMeta(NCSynonymDslParser.ModelMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#modelMeta}.
	 * @param ctx the parse tree
	 */
	void exitModelMeta(NCSynonymDslParser.ModelMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#intentMeta}.
	 * @param ctx the parse tree
	 */
	void enterIntentMeta(NCSynonymDslParser.IntentMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#intentMeta}.
	 * @param ctx the parse tree
	 */
	void exitIntentMeta(NCSynonymDslParser.IntentMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#qstring}.
	 * @param ctx the parse tree
	 */
	void enterQstring(NCSynonymDslParser.QstringContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#qstring}.
	 * @param ctx the parse tree
	 */
	void exitQstring(NCSynonymDslParser.QstringContext ctx);
}