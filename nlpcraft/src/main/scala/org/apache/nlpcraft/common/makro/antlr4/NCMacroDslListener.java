// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/common/makro/antlr4\NCMacroDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.common.makro.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link NCMacroDslParser}.
 */
public interface NCMacroDslListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#makro}.
	 * @param ctx the parse tree
	 */
	void enterMakro(NCMacroDslParser.MakroContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#makro}.
	 * @param ctx the parse tree
	 */
	void exitMakro(NCMacroDslParser.MakroContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExpr(NCMacroDslParser.ExprContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExpr(NCMacroDslParser.ExprContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#item}.
	 * @param ctx the parse tree
	 */
	void enterItem(NCMacroDslParser.ItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#item}.
	 * @param ctx the parse tree
	 */
	void exitItem(NCMacroDslParser.ItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#syn}.
	 * @param ctx the parse tree
	 */
	void enterSyn(NCMacroDslParser.SynContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#syn}.
	 * @param ctx the parse tree
	 */
	void exitSyn(NCMacroDslParser.SynContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#group}.
	 * @param ctx the parse tree
	 */
	void enterGroup(NCMacroDslParser.GroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#group}.
	 * @param ctx the parse tree
	 */
	void exitGroup(NCMacroDslParser.GroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#list}.
	 * @param ctx the parse tree
	 */
	void enterList(NCMacroDslParser.ListContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#list}.
	 * @param ctx the parse tree
	 */
	void exitList(NCMacroDslParser.ListContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#minMax}.
	 * @param ctx the parse tree
	 */
	void enterMinMax(NCMacroDslParser.MinMaxContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#minMax}.
	 * @param ctx the parse tree
	 */
	void exitMinMax(NCMacroDslParser.MinMaxContext ctx);
	/**
	 * Enter a parse tree produced by {@link NCMacroDslParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void enterMinMaxShortcut(NCMacroDslParser.MinMaxShortcutContext ctx);
	/**
	 * Exit a parse tree produced by {@link NCMacroDslParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 */
	void exitMinMaxShortcut(NCMacroDslParser.MinMaxShortcutContext ctx);
}