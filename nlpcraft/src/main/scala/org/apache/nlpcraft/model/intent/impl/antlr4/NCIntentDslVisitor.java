// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4/NCIntentDsl.g4 by ANTLR 4.8
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link NCIntentDslParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface NCIntentDslVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#intent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntent(NCIntentDslParser.IntentContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#intentId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntentId(NCIntentDslParser.IntentIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#convDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConvDecl(NCIntentDslParser.ConvDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#orderedDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedDecl(NCIntentDslParser.OrderedDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#flowDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlowDecl(NCIntentDslParser.FlowDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#flow}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlow(NCIntentDslParser.FlowContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#flowItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlowItem(NCIntentDslParser.FlowItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#flowItemIds}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlowItemIds(NCIntentDslParser.FlowItemIdsContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#idList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdList(NCIntentDslParser.IdListContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerms(NCIntentDslParser.TermsContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(NCIntentDslParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#termId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermId(NCIntentDslParser.TermIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem(NCIntentDslParser.ItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(NCIntentDslParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#lval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLval(NCIntentDslParser.LvalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#lvalQual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalQual(NCIntentDslParser.LvalQualContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#lvalPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalPart(NCIntentDslParser.LvalPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#rvalSingle}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRvalSingle(NCIntentDslParser.RvalSingleContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#rval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRval(NCIntentDslParser.RvalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#rvalList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRvalList(NCIntentDslParser.RvalListContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#meta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeta(NCIntentDslParser.MetaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#qstring}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQstring(NCIntentDslParser.QstringContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#minMax}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinMax(NCIntentDslParser.MinMaxContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinMaxShortcut(NCIntentDslParser.MinMaxShortcutContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#minMaxRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinMaxRange(NCIntentDslParser.MinMaxRangeContext ctx);
}