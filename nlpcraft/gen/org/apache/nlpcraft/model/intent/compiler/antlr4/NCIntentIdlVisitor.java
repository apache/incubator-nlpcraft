// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/compiler/antlr4\NCIntentIdl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link NCIntentIdlParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface NCIntentIdlVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#idl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdl(NCIntentIdlParser.IdlContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#synonym}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSynonym(NCIntentIdlParser.SynonymContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#alias}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAlias(NCIntentIdlParser.AliasContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#idlItems}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdlItems(NCIntentIdlParser.IdlItemsContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#idlItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIdlItem(NCIntentIdlParser.IdlItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#imp}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitImp(NCIntentIdlParser.ImpContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#frag}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFrag(NCIntentIdlParser.FragContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#fragId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragId(NCIntentIdlParser.FragIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#fragRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragRef(NCIntentIdlParser.FragRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#fragMeta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFragMeta(NCIntentIdlParser.FragMetaContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#intent}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntent(NCIntentIdlParser.IntentContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#intentId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntentId(NCIntentIdlParser.IntentIdContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#orderedDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOrderedDecl(NCIntentIdlParser.OrderedDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#mtdDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMtdDecl(NCIntentIdlParser.MtdDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#flowDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFlowDecl(NCIntentIdlParser.FlowDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#metaDecl}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetaDecl(NCIntentIdlParser.MetaDeclContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#jsonObj}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonObj(NCIntentIdlParser.JsonObjContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#jsonPair}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonPair(NCIntentIdlParser.JsonPairContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#jsonVal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonVal(NCIntentIdlParser.JsonValContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#jsonArr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJsonArr(NCIntentIdlParser.JsonArrContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#terms}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerms(NCIntentIdlParser.TermsContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#termItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermItem(NCIntentIdlParser.TermItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#termEq}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermEq(NCIntentIdlParser.TermEqContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#term}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTerm(NCIntentIdlParser.TermContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#mtdRef}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMtdRef(NCIntentIdlParser.MtdRefContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#javaFqn}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJavaFqn(NCIntentIdlParser.JavaFqnContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#termId}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTermId(NCIntentIdlParser.TermIdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParExpr(NCIntentIdlParser.ParExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryExpr(NCIntentIdlParser.UnaryExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code compExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompExpr(NCIntentIdlParser.CompExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code plusMinusExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPlusMinusExpr(NCIntentIdlParser.PlusMinusExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code atomExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtomExpr(NCIntentIdlParser.AtomExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code multDivModExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultDivModExpr(NCIntentIdlParser.MultDivModExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code andOrExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAndOrExpr(NCIntentIdlParser.AndOrExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code callExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCallExpr(NCIntentIdlParser.CallExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code eqNeqExpr}
	 * labeled alternative in {@link NCIntentIdlParser#expr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqNeqExpr(NCIntentIdlParser.EqNeqExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#paramList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParamList(NCIntentIdlParser.ParamListContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#atom}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAtom(NCIntentIdlParser.AtomContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#qstring}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitQstring(NCIntentIdlParser.QstringContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#minMax}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinMax(NCIntentIdlParser.MinMaxContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#minMaxShortcut}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinMaxShortcut(NCIntentIdlParser.MinMaxShortcutContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#minMaxRange}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMinMaxRange(NCIntentIdlParser.MinMaxRangeContext ctx);
	/**
	 * Visit a parse tree produced by {@link NCIntentIdlParser#id}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(NCIntentIdlParser.IdContext ctx);
}