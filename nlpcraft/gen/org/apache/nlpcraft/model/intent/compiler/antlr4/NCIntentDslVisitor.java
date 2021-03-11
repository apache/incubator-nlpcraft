// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/compiler/antlr4/NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
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
	 * Visit a parse tree produced by {@link NCIntentDslParser#dsl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDsl(NCIntentDslParser.DslContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#synonym}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynonym(NCIntentDslParser.SynonymContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(NCIntentDslParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#dslItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDslItems(NCIntentDslParser.DslItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#dslItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDslItem(NCIntentDslParser.DslItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#url}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUrl(NCIntentDslParser.UrlContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#frag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrag(NCIntentDslParser.FragContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#fragId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragId(NCIntentDslParser.FragIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#fragRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragRef(NCIntentDslParser.FragRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#fragMeta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragMeta(NCIntentDslParser.FragMetaContext ctx);
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
	 * Visit a parse tree produced by {@link NCIntentDslParser#orderedDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedDecl(NCIntentDslParser.OrderedDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#mtdDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMtdDecl(NCIntentDslParser.MtdDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#flowDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlowDecl(NCIntentDslParser.FlowDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#metaDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetaDecl(NCIntentDslParser.MetaDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#jsonObj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonObj(NCIntentDslParser.JsonObjContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#jsonPair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPair(NCIntentDslParser.JsonPairContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#jsonVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonVal(NCIntentDslParser.JsonValContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#jsonArr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonArr(NCIntentDslParser.JsonArrContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerms(NCIntentDslParser.TermsContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#termItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermItem(NCIntentDslParser.TermItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#termEq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermEq(NCIntentDslParser.TermEqContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(NCIntentDslParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#mtdRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMtdRef(NCIntentDslParser.MtdRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#javaFqn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJavaFqn(NCIntentDslParser.JavaFqnContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#termId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermId(NCIntentDslParser.TermIdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParExpr(NCIntentDslParser.ParExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code eqExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqExpr(NCIntentDslParser.EqExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(NCIntentDslParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompExpr(NCIntentDslParser.CompExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(NCIntentDslParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallExpr(NCIntentDslParser.CallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultExpr(NCIntentDslParser.MultExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code plusExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlusExpr(NCIntentDslParser.PlusExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code logExpr}
	 * labeled alternative in {@link NCIntentDslParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogExpr(NCIntentDslParser.LogExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(NCIntentDslParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(NCIntentDslParser.AtomContext ctx);
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
	/**
	 * Visit a parse tree produced by {@link NCIntentDslParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(NCIntentDslParser.IdContext ctx);
}