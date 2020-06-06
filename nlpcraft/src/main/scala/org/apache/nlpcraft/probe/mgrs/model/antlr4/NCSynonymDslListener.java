// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4/NCSynonymDsl.g4 by ANTLR 4.8
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
	 * Enter a parse tree produced by {@link NCSynonymDslParser#predicate}.
	 * @param ctx the parse tree
	 */
	void enterPredicate(NCSynonymDslParser.PredicateContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#predicate}.
	 * @param ctx the parse tree
	 */
	void exitPredicate(NCSynonymDslParser.PredicateContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#lval}.
	 * @param ctx the parse tree
	 */
	void enterLval(NCSynonymDslParser.LvalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#lval}.
	 * @param ctx the parse tree
	 */
	void exitLval(NCSynonymDslParser.LvalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#lvalQual}.
	 * @param ctx the parse tree
	 */
	void enterLvalQual(NCSynonymDslParser.LvalQualContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#lvalQual}.
	 * @param ctx the parse tree
	 */
	void exitLvalQual(NCSynonymDslParser.LvalQualContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#lvalPart}.
	 * @param ctx the parse tree
	 */
	void enterLvalPart(NCSynonymDslParser.LvalPartContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#lvalPart}.
	 * @param ctx the parse tree
	 */
	void exitLvalPart(NCSynonymDslParser.LvalPartContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#rvalSingle}.
	 * @param ctx the parse tree
	 */
	void enterRvalSingle(NCSynonymDslParser.RvalSingleContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#rvalSingle}.
	 * @param ctx the parse tree
	 */
	void exitRvalSingle(NCSynonymDslParser.RvalSingleContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#rval}.
	 * @param ctx the parse tree
	 */
	void enterRval(NCSynonymDslParser.RvalContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#rval}.
	 * @param ctx the parse tree
	 */
	void exitRval(NCSynonymDslParser.RvalContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#rvalList}.
	 * @param ctx the parse tree
	 */
	void enterRvalList(NCSynonymDslParser.RvalListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#rvalList}.
	 * @param ctx the parse tree
	 */
	void exitRvalList(NCSynonymDslParser.RvalListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCSynonymDslParser#meta}.
	 * @param ctx the parse tree
	 */
	void enterMeta(NCSynonymDslParser.MetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCSynonymDslParser#meta}.
	 * @param ctx the parse tree
	 */
	void exitMeta(NCSynonymDslParser.MetaContext ctx);
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