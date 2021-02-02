// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9.1
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#metaList}.
	 * @param ctx the parse tree
	 */
	void enterMetaList(NCIntentDslParser.MetaListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#metaList}.
	 * @param ctx the parse tree
	 */
	void exitMetaList(NCIntentDslParser.MetaListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#metaItem}.
	 * @param ctx the parse tree
	 */
	void enterMetaItem(NCIntentDslParser.MetaItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#metaItem}.
	 * @param ctx the parse tree
	 */
	void exitMetaItem(NCIntentDslParser.MetaItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#metaItemRval}.
	 * @param ctx the parse tree
	 */
	void enterMetaItemRval(NCIntentDslParser.MetaItemRvalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#metaItemRval}.
	 * @param ctx the parse tree
	 */
	void exitMetaItemRval(NCIntentDslParser.MetaItemRvalContext ctx);
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#item}.
	 * @param ctx the parse tree
	 */
	void enterItem(NCIntentDslParser.ItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#item}.
	 * @param ctx the parse tree
	 */
	void exitItem(NCIntentDslParser.ItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#pred}.
	 * @param ctx the parse tree
	 */
	void enterPred(NCIntentDslParser.PredContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#pred}.
	 * @param ctx the parse tree
	 */
	void exitPred(NCIntentDslParser.PredContext ctx);
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#val}.
	 * @param ctx the parse tree
	 */
	void enterVal(NCIntentDslParser.ValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#val}.
	 * @param ctx the parse tree
	 */
	void exitVal(NCIntentDslParser.ValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#singleVal}.
	 * @param ctx the parse tree
	 */
	void enterSingleVal(NCIntentDslParser.SingleValContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#singleVal}.
	 * @param ctx the parse tree
	 */
	void exitSingleVal(NCIntentDslParser.SingleValContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#tokQual}.
	 * @param ctx the parse tree
	 */
	void enterTokQual(NCIntentDslParser.TokQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#tokQual}.
	 * @param ctx the parse tree
	 */
	void exitTokQual(NCIntentDslParser.TokQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#tokQualPart}.
	 * @param ctx the parse tree
	 */
	void enterTokQualPart(NCIntentDslParser.TokQualPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#tokQualPart}.
	 * @param ctx the parse tree
	 */
	void exitTokQualPart(NCIntentDslParser.TokQualPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#tokMeta}.
	 * @param ctx the parse tree
	 */
	void enterTokMeta(NCIntentDslParser.TokMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#tokMeta}.
	 * @param ctx the parse tree
	 */
	void exitTokMeta(NCIntentDslParser.TokMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#modelMeta}.
	 * @param ctx the parse tree
	 */
	void enterModelMeta(NCIntentDslParser.ModelMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#modelMeta}.
	 * @param ctx the parse tree
	 */
	void exitModelMeta(NCIntentDslParser.ModelMetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#intentMeta}.
	 * @param ctx the parse tree
	 */
	void enterIntentMeta(NCIntentDslParser.IntentMetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#intentMeta}.
	 * @param ctx the parse tree
	 */
	void exitIntentMeta(NCIntentDslParser.IntentMetaContext ctx);
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