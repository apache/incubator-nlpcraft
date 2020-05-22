// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4/NCSynonymDsl.g4 by ANTLR 4.8
package org.apache.nlpcraft.probe.mgrs.model.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link NCSynonymDslParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface NCSynonymDslVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#synonym}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynonym(NCSynonymDslParser.SynonymContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(NCSynonymDslParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#item}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitItem(NCSynonymDslParser.ItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#predicate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPredicate(NCSynonymDslParser.PredicateContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#lval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLval(NCSynonymDslParser.LvalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#lvalQual}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalQual(NCSynonymDslParser.LvalQualContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#lvalPart}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalPart(NCSynonymDslParser.LvalPartContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#rvalSingle}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRvalSingle(NCSynonymDslParser.RvalSingleContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#rval}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRval(NCSynonymDslParser.RvalContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#rvalList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRvalList(NCSynonymDslParser.RvalListContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#meta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeta(NCSynonymDslParser.MetaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCSynonymDslParser#qstring}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQstring(NCSynonymDslParser.QstringContext ctx);
}