// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4/NCIntentDsl.g4 by ANTLR 4.8
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#convDecl}.
	 * @param ctx the parse tree
	 */
	void enterConvDecl(NCIntentDslParser.ConvDeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#convDecl}.
	 * @param ctx the parse tree
	 */
	void exitConvDecl(NCIntentDslParser.ConvDeclContext ctx);
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#flow}.
	 * @param ctx the parse tree
	 */
	void enterFlow(NCIntentDslParser.FlowContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#flow}.
	 * @param ctx the parse tree
	 */
	void exitFlow(NCIntentDslParser.FlowContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#flowItem}.
	 * @param ctx the parse tree
	 */
	void enterFlowItem(NCIntentDslParser.FlowItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#flowItem}.
	 * @param ctx the parse tree
	 */
	void exitFlowItem(NCIntentDslParser.FlowItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#flowItemIds}.
	 * @param ctx the parse tree
	 */
	void enterFlowItemIds(NCIntentDslParser.FlowItemIdsContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#flowItemIds}.
	 * @param ctx the parse tree
	 */
	void exitFlowItemIds(NCIntentDslParser.FlowItemIdsContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#idList}.
	 * @param ctx the parse tree
	 */
	void enterIdList(NCIntentDslParser.IdListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#idList}.
	 * @param ctx the parse tree
	 */
	void exitIdList(NCIntentDslParser.IdListContext ctx);
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
	 * Enter a parse tree produced by {@link NCIntentDslParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(NCIntentDslParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(NCIntentDslParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#lval}.
	 * @param ctx the parse tree
	 */
	void enterLval(NCIntentDslParser.LvalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#lval}.
	 * @param ctx the parse tree
	 */
	void exitLval(NCIntentDslParser.LvalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#lvalQual}.
	 * @param ctx the parse tree
	 */
	void enterLvalQual(NCIntentDslParser.LvalQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#lvalQual}.
	 * @param ctx the parse tree
	 */
	void exitLvalQual(NCIntentDslParser.LvalQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#lvalPart}.
	 * @param ctx the parse tree
	 */
	void enterLvalPart(NCIntentDslParser.LvalPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#lvalPart}.
	 * @param ctx the parse tree
	 */
	void exitLvalPart(NCIntentDslParser.LvalPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#rvalSingle}.
	 * @param ctx the parse tree
	 */
	void enterRvalSingle(NCIntentDslParser.RvalSingleContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#rvalSingle}.
	 * @param ctx the parse tree
	 */
	void exitRvalSingle(NCIntentDslParser.RvalSingleContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#rval}.
	 * @param ctx the parse tree
	 */
	void enterRval(NCIntentDslParser.RvalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#rval}.
	 * @param ctx the parse tree
	 */
	void exitRval(NCIntentDslParser.RvalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#rvalList}.
	 * @param ctx the parse tree
	 */
	void enterRvalList(NCIntentDslParser.RvalListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#rvalList}.
	 * @param ctx the parse tree
	 */
	void exitRvalList(NCIntentDslParser.RvalListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCIntentDslParser#meta}.
	 * @param ctx the parse tree
	 */
	void enterMeta(NCIntentDslParser.MetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCIntentDslParser#meta}.
	 * @param ctx the parse tree
	 */
	void exitMeta(NCIntentDslParser.MetaContext ctx);
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