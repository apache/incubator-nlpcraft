// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/probe/mgrs/model/antlr4\NCSynonymDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.probe.mgrs.model.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCSynonymDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		SQSTRING=10, DQSTRING=11, PRED_OP=12, AND=13, OR=14, EXCL=15, LPAREN=16, 
		RPAREN=17, SQUOTE=18, DQUOTE=19, DOLLAR=20, TILDA=21, LBR=22, RBR=23, 
		PLUS=24, STAR=25, DEVIDE=26, COMMA=27, COLON=28, POUND=29, MINUS=30, DOT=31, 
		UNDERSCORE=32, BOOL=33, INT=34, EXP=35, ID=36, WS=37, ErrorCharacter=38;
	public static final int
		RULE_synonym = 0, RULE_alias = 1, RULE_item = 2, RULE_pred = 3, RULE_expr = 4, 
		RULE_mathOp = 5, RULE_val = 6, RULE_singleVal = 7, RULE_tokQual = 8, RULE_tokQualPart = 9, 
		RULE_tokMeta = 10, RULE_modelMeta = 11, RULE_intentMeta = 12, RULE_qstring = 13;
	private static String[] makeRuleNames() {
		return new String[] {
			"synonym", "alias", "item", "pred", "expr", "mathOp", "val", "singleVal", 
			"tokQual", "tokQualPart", "tokMeta", "modelMeta", "intentMeta", "qstring"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'null'", "'id'", "'aliases'", "'startidx'", "'endidx'", "'parent'", 
			"'groups'", "'ancestors'", "'value'", null, null, null, "'&&'", "'||'", 
			"'!'", "'('", "')'", "'''", "'\"'", "'$'", "'~'", "'['", "']'", "'+'", 
			"'*'", "'/'", "','", "':'", "'#'", "'-'", "'.'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "SQSTRING", 
			"DQSTRING", "PRED_OP", "AND", "OR", "EXCL", "LPAREN", "RPAREN", "SQUOTE", 
			"DQUOTE", "DOLLAR", "TILDA", "LBR", "RBR", "PLUS", "STAR", "DEVIDE", 
			"COMMA", "COLON", "POUND", "MINUS", "DOT", "UNDERSCORE", "BOOL", "INT", 
			"EXP", "ID", "WS", "ErrorCharacter"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "NCSynonymDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCSynonymDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class SynonymContext extends ParserRuleContext {
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public TerminalNode EOF() { return getToken(NCSynonymDslParser.EOF, 0); }
		public SynonymContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_synonym; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterSynonym(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitSynonym(this);
		}
	}

	public final SynonymContext synonym() throws RecognitionException {
		SynonymContext _localctx = new SynonymContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_synonym);
		try {
			setState(37);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBR:
				enterOuterAlt(_localctx, 1);
				{
				setState(28);
				alias();
				setState(29);
				match(LPAREN);
				setState(30);
				item(0);
				setState(31);
				match(RPAREN);
				setState(32);
				match(EOF);
				}
				break;
			case T__0:
			case T__1:
			case T__2:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case SQSTRING:
			case DQSTRING:
			case EXCL:
			case LPAREN:
			case DOLLAR:
			case TILDA:
			case POUND:
			case MINUS:
			case BOOL:
			case INT:
			case ID:
				enterOuterAlt(_localctx, 2);
				{
				setState(34);
				item(0);
				setState(35);
				match(EOF);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AliasContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCSynonymDslParser.LBR, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode RBR() { return getToken(NCSynonymDslParser.RBR, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitAlias(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(39);
			match(LBR);
			setState(40);
			match(ID);
			setState(41);
			match(RBR);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ItemContext extends ParserRuleContext {
		public PredContext pred() {
			return getRuleContext(PredContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public TerminalNode EXCL() { return getToken(NCSynonymDslParser.EXCL, 0); }
		public TerminalNode AND() { return getToken(NCSynonymDslParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCSynonymDslParser.OR, 0); }
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitItem(this);
		}
	}

	public final ItemContext item() throws RecognitionException {
		return item(0);
	}

	private ItemContext item(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ItemContext _localctx = new ItemContext(_ctx, _parentState);
		ItemContext _prevctx = _localctx;
		int _startState = 4;
		enterRecursionRule(_localctx, 4, RULE_item, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(51);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(44);
				pred();
				}
				break;
			case 2:
				{
				setState(45);
				match(LPAREN);
				setState(46);
				item(0);
				setState(47);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(49);
				match(EXCL);
				setState(50);
				item(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(58);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ItemContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_item);
					setState(53);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(54);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(55);
					item(3);
					}
					} 
				}
				setState(60);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class PredContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode PRED_OP() { return getToken(NCSynonymDslParser.PRED_OP, 0); }
		public PredContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterPred(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitPred(this);
		}
	}

	public final PredContext pred() throws RecognitionException {
		PredContext _localctx = new PredContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_pred);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(61);
			expr(0);
			setState(62);
			match(PRED_OP);
			setState(63);
			expr(0);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExprContext extends ParserRuleContext {
		public ValContext val() {
			return getRuleContext(ValContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public MathOpContext mathOp() {
			return getRuleContext(MathOpContext.class,0);
		}
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitExpr(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(66);
				val(0);
				}
				break;
			case 2:
				{
				setState(67);
				match(LPAREN);
				setState(68);
				expr(0);
				setState(69);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(71);
				match(ID);
				setState(72);
				match(LPAREN);
				setState(74);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << SQSTRING) | (1L << DQSTRING) | (1L << LPAREN) | (1L << DOLLAR) | (1L << TILDA) | (1L << POUND) | (1L << MINUS) | (1L << BOOL) | (1L << INT) | (1L << ID))) != 0)) {
					{
					setState(73);
					expr(0);
					}
				}

				setState(76);
				match(RPAREN);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(85);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(79);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(80);
					mathOp();
					setState(81);
					expr(3);
					}
					} 
				}
				setState(87);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class MathOpContext extends ParserRuleContext {
		public TerminalNode MINUS() { return getToken(NCSynonymDslParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(NCSynonymDslParser.PLUS, 0); }
		public TerminalNode STAR() { return getToken(NCSynonymDslParser.STAR, 0); }
		public TerminalNode DEVIDE() { return getToken(NCSynonymDslParser.DEVIDE, 0); }
		public MathOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterMathOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitMathOp(this);
		}
	}

	public final MathOpContext mathOp() throws RecognitionException {
		MathOpContext _localctx = new MathOpContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_mathOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << STAR) | (1L << DEVIDE) | (1L << MINUS))) != 0)) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValContext extends ParserRuleContext {
		public SingleValContext singleVal() {
			return getRuleContext(SingleValContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public List<ValContext> val() {
			return getRuleContexts(ValContext.class);
		}
		public ValContext val(int i) {
			return getRuleContext(ValContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(NCSynonymDslParser.COMMA, 0); }
		public ValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_val; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitVal(this);
		}
	}

	public final ValContext val() throws RecognitionException {
		return val(0);
	}

	private ValContext val(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ValContext _localctx = new ValContext(_ctx, _parentState);
		ValContext _prevctx = _localctx;
		int _startState = 12;
		enterRecursionRule(_localctx, 12, RULE_val, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
			case T__2:
			case T__3:
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case SQSTRING:
			case DQSTRING:
			case DOLLAR:
			case TILDA:
			case POUND:
			case MINUS:
			case BOOL:
			case INT:
			case ID:
				{
				setState(91);
				singleVal();
				}
				break;
			case LPAREN:
				{
				setState(92);
				match(LPAREN);
				setState(93);
				val(0);
				setState(94);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(103);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ValContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_val);
					setState(98);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(99);
					match(COMMA);
					setState(100);
					val(2);
					}
					} 
				}
				setState(105);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class SingleValContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(NCSynonymDslParser.INT, 0); }
		public TerminalNode EXP() { return getToken(NCSynonymDslParser.EXP, 0); }
		public TerminalNode MINUS() { return getToken(NCSynonymDslParser.MINUS, 0); }
		public TerminalNode BOOL() { return getToken(NCSynonymDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TokQualContext tokQual() {
			return getRuleContext(TokQualContext.class,0);
		}
		public TokMetaContext tokMeta() {
			return getRuleContext(TokMetaContext.class,0);
		}
		public ModelMetaContext modelMeta() {
			return getRuleContext(ModelMetaContext.class,0);
		}
		public IntentMetaContext intentMeta() {
			return getRuleContext(IntentMetaContext.class,0);
		}
		public SingleValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singleVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterSingleVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitSingleVal(this);
		}
	}

	public final SingleValContext singleVal() throws RecognitionException {
		SingleValContext _localctx = new SingleValContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_singleVal);
		int _la;
		try {
			setState(127);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(106);
				match(T__0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(108);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(107);
					match(MINUS);
					}
				}

				setState(113);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
				case 1:
					{
					setState(110);
					match(INT);
					}
					break;
				case 2:
					{
					setState(111);
					match(INT);
					setState(112);
					match(EXP);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(115);
				match(BOOL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(116);
				qstring();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(118);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(117);
					tokQual(0);
					}
				}

				setState(120);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(122);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(121);
					tokQual(0);
					}
				}

				setState(124);
				tokMeta();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(125);
				modelMeta();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(126);
				intentMeta();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TokQualContext extends ParserRuleContext {
		public TokQualPartContext tokQualPart() {
			return getRuleContext(TokQualPartContext.class,0);
		}
		public TokQualContext tokQual() {
			return getRuleContext(TokQualContext.class,0);
		}
		public TokQualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokQual; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterTokQual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitTokQual(this);
		}
	}

	public final TokQualContext tokQual() throws RecognitionException {
		return tokQual(0);
	}

	private TokQualContext tokQual(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TokQualContext _localctx = new TokQualContext(_ctx, _parentState);
		TokQualContext _prevctx = _localctx;
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_tokQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(130);
			tokQualPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(136);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TokQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_tokQual);
					setState(132);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(133);
					tokQualPart();
					}
					} 
				}
				setState(138);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class TokQualPartContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode DOT() { return getToken(NCSynonymDslParser.DOT, 0); }
		public TokQualPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokQualPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterTokQualPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitTokQualPart(this);
		}
	}

	public final TokQualPartContext tokQualPart() throws RecognitionException {
		TokQualPartContext _localctx = new TokQualPartContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_tokQualPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(ID);
			setState(140);
			match(DOT);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TokMetaContext extends ParserRuleContext {
		public TerminalNode TILDA() { return getToken(NCSynonymDslParser.TILDA, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCSynonymDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCSynonymDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCSynonymDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TokMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterTokMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitTokMeta(this);
		}
	}

	public final TokMetaContext tokMeta() throws RecognitionException {
		TokMetaContext _localctx = new TokMetaContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_tokMeta);
		try {
			setState(155);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(142);
				match(TILDA);
				setState(143);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(144);
				match(TILDA);
				setState(145);
				match(ID);
				setState(146);
				match(LBR);
				setState(147);
				match(INT);
				setState(148);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(149);
				match(TILDA);
				setState(150);
				match(ID);
				setState(151);
				match(LBR);
				setState(152);
				qstring();
				setState(153);
				match(RBR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModelMetaContext extends ParserRuleContext {
		public TerminalNode POUND() { return getToken(NCSynonymDslParser.POUND, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCSynonymDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCSynonymDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCSynonymDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public ModelMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modelMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterModelMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitModelMeta(this);
		}
	}

	public final ModelMetaContext modelMeta() throws RecognitionException {
		ModelMetaContext _localctx = new ModelMetaContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_modelMeta);
		try {
			setState(170);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(157);
				match(POUND);
				setState(158);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(159);
				match(POUND);
				setState(160);
				match(ID);
				setState(161);
				match(LBR);
				setState(162);
				match(INT);
				setState(163);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(164);
				match(POUND);
				setState(165);
				match(ID);
				setState(166);
				match(LBR);
				setState(167);
				qstring();
				setState(168);
				match(RBR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntentMetaContext extends ParserRuleContext {
		public TerminalNode DOLLAR() { return getToken(NCSynonymDslParser.DOLLAR, 0); }
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCSynonymDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCSynonymDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCSynonymDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public IntentMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intentMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterIntentMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitIntentMeta(this);
		}
	}

	public final IntentMetaContext intentMeta() throws RecognitionException {
		IntentMetaContext _localctx = new IntentMetaContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_intentMeta);
		try {
			setState(185);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(172);
				match(DOLLAR);
				setState(173);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(174);
				match(DOLLAR);
				setState(175);
				match(ID);
				setState(176);
				match(LBR);
				setState(177);
				match(INT);
				setState(178);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(179);
				match(DOLLAR);
				setState(180);
				match(ID);
				setState(181);
				match(LBR);
				setState(182);
				qstring();
				setState(183);
				match(RBR);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QstringContext extends ParserRuleContext {
		public TerminalNode SQSTRING() { return getToken(NCSynonymDslParser.SQSTRING, 0); }
		public TerminalNode DQSTRING() { return getToken(NCSynonymDslParser.DQSTRING, 0); }
		public QstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).enterQstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCSynonymDslListener ) ((NCSynonymDslListener)listener).exitQstring(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			_la = _input.LA(1);
			if ( !(_la==SQSTRING || _la==DQSTRING) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return item_sempred((ItemContext)_localctx, predIndex);
		case 4:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 6:
			return val_sempred((ValContext)_localctx, predIndex);
		case 8:
			return tokQual_sempred((TokQualContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean item_sempred(ItemContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean val_sempred(ValContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean tokQual_sempred(TokQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3(\u00c0\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3"+
		"\2\3\2\5\2(\n\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\66"+
		"\n\4\3\4\3\4\3\4\7\4;\n\4\f\4\16\4>\13\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3"+
		"\6\3\6\3\6\3\6\3\6\3\6\5\6M\n\6\3\6\5\6P\n\6\3\6\3\6\3\6\3\6\7\6V\n\6"+
		"\f\6\16\6Y\13\6\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\5\bc\n\b\3\b\3\b\3\b\7"+
		"\bh\n\b\f\b\16\bk\13\b\3\t\3\t\5\to\n\t\3\t\3\t\3\t\5\tt\n\t\3\t\3\t\3"+
		"\t\5\ty\n\t\3\t\3\t\5\t}\n\t\3\t\3\t\3\t\5\t\u0082\n\t\3\n\3\n\3\n\3\n"+
		"\3\n\7\n\u0089\n\n\f\n\16\n\u008c\13\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u009e\n\f\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u00ad\n\r\3\16\3\16\3\16\3\16\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u00bc\n\16\3\17\3\17\3\17"+
		"\2\6\6\n\16\22\20\2\4\6\b\n\f\16\20\22\24\26\30\32\34\2\6\3\2\17\20\4"+
		"\2\32\34  \3\2\4\13\3\2\f\r\2\u00cd\2\'\3\2\2\2\4)\3\2\2\2\6\65\3\2\2"+
		"\2\b?\3\2\2\2\nO\3\2\2\2\fZ\3\2\2\2\16b\3\2\2\2\20\u0081\3\2\2\2\22\u0083"+
		"\3\2\2\2\24\u008d\3\2\2\2\26\u009d\3\2\2\2\30\u00ac\3\2\2\2\32\u00bb\3"+
		"\2\2\2\34\u00bd\3\2\2\2\36\37\5\4\3\2\37 \7\22\2\2 !\5\6\4\2!\"\7\23\2"+
		"\2\"#\7\2\2\3#(\3\2\2\2$%\5\6\4\2%&\7\2\2\3&(\3\2\2\2\'\36\3\2\2\2\'$"+
		"\3\2\2\2(\3\3\2\2\2)*\7\30\2\2*+\7&\2\2+,\7\31\2\2,\5\3\2\2\2-.\b\4\1"+
		"\2.\66\5\b\5\2/\60\7\22\2\2\60\61\5\6\4\2\61\62\7\23\2\2\62\66\3\2\2\2"+
		"\63\64\7\21\2\2\64\66\5\6\4\3\65-\3\2\2\2\65/\3\2\2\2\65\63\3\2\2\2\66"+
		"<\3\2\2\2\678\f\4\2\289\t\2\2\29;\5\6\4\5:\67\3\2\2\2;>\3\2\2\2<:\3\2"+
		"\2\2<=\3\2\2\2=\7\3\2\2\2><\3\2\2\2?@\5\n\6\2@A\7\16\2\2AB\5\n\6\2B\t"+
		"\3\2\2\2CD\b\6\1\2DP\5\16\b\2EF\7\22\2\2FG\5\n\6\2GH\7\23\2\2HP\3\2\2"+
		"\2IJ\7&\2\2JL\7\22\2\2KM\5\n\6\2LK\3\2\2\2LM\3\2\2\2MN\3\2\2\2NP\7\23"+
		"\2\2OC\3\2\2\2OE\3\2\2\2OI\3\2\2\2PW\3\2\2\2QR\f\4\2\2RS\5\f\7\2ST\5\n"+
		"\6\5TV\3\2\2\2UQ\3\2\2\2VY\3\2\2\2WU\3\2\2\2WX\3\2\2\2X\13\3\2\2\2YW\3"+
		"\2\2\2Z[\t\3\2\2[\r\3\2\2\2\\]\b\b\1\2]c\5\20\t\2^_\7\22\2\2_`\5\16\b"+
		"\2`a\7\23\2\2ac\3\2\2\2b\\\3\2\2\2b^\3\2\2\2ci\3\2\2\2de\f\3\2\2ef\7\35"+
		"\2\2fh\5\16\b\4gd\3\2\2\2hk\3\2\2\2ig\3\2\2\2ij\3\2\2\2j\17\3\2\2\2ki"+
		"\3\2\2\2l\u0082\7\3\2\2mo\7 \2\2nm\3\2\2\2no\3\2\2\2os\3\2\2\2pt\7$\2"+
		"\2qr\7$\2\2rt\7%\2\2sp\3\2\2\2sq\3\2\2\2t\u0082\3\2\2\2u\u0082\7#\2\2"+
		"v\u0082\5\34\17\2wy\5\22\n\2xw\3\2\2\2xy\3\2\2\2yz\3\2\2\2z\u0082\t\4"+
		"\2\2{}\5\22\n\2|{\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\u0082\5\26\f\2\177\u0082"+
		"\5\30\r\2\u0080\u0082\5\32\16\2\u0081l\3\2\2\2\u0081n\3\2\2\2\u0081u\3"+
		"\2\2\2\u0081v\3\2\2\2\u0081x\3\2\2\2\u0081|\3\2\2\2\u0081\177\3\2\2\2"+
		"\u0081\u0080\3\2\2\2\u0082\21\3\2\2\2\u0083\u0084\b\n\1\2\u0084\u0085"+
		"\5\24\13\2\u0085\u008a\3\2\2\2\u0086\u0087\f\3\2\2\u0087\u0089\5\24\13"+
		"\2\u0088\u0086\3\2\2\2\u0089\u008c\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b"+
		"\3\2\2\2\u008b\23\3\2\2\2\u008c\u008a\3\2\2\2\u008d\u008e\7&\2\2\u008e"+
		"\u008f\7!\2\2\u008f\25\3\2\2\2\u0090\u0091\7\27\2\2\u0091\u009e\7&\2\2"+
		"\u0092\u0093\7\27\2\2\u0093\u0094\7&\2\2\u0094\u0095\7\30\2\2\u0095\u0096"+
		"\7$\2\2\u0096\u009e\7\31\2\2\u0097\u0098\7\27\2\2\u0098\u0099\7&\2\2\u0099"+
		"\u009a\7\30\2\2\u009a\u009b\5\34\17\2\u009b\u009c\7\31\2\2\u009c\u009e"+
		"\3\2\2\2\u009d\u0090\3\2\2\2\u009d\u0092\3\2\2\2\u009d\u0097\3\2\2\2\u009e"+
		"\27\3\2\2\2\u009f\u00a0\7\37\2\2\u00a0\u00ad\7&\2\2\u00a1\u00a2\7\37\2"+
		"\2\u00a2\u00a3\7&\2\2\u00a3\u00a4\7\30\2\2\u00a4\u00a5\7$\2\2\u00a5\u00ad"+
		"\7\31\2\2\u00a6\u00a7\7\37\2\2\u00a7\u00a8\7&\2\2\u00a8\u00a9\7\30\2\2"+
		"\u00a9\u00aa\5\34\17\2\u00aa\u00ab\7\31\2\2\u00ab\u00ad\3\2\2\2\u00ac"+
		"\u009f\3\2\2\2\u00ac\u00a1\3\2\2\2\u00ac\u00a6\3\2\2\2\u00ad\31\3\2\2"+
		"\2\u00ae\u00af\7\26\2\2\u00af\u00bc\7&\2\2\u00b0\u00b1\7\26\2\2\u00b1"+
		"\u00b2\7&\2\2\u00b2\u00b3\7\30\2\2\u00b3\u00b4\7$\2\2\u00b4\u00bc\7\31"+
		"\2\2\u00b5\u00b6\7\26\2\2\u00b6\u00b7\7&\2\2\u00b7\u00b8\7\30\2\2\u00b8"+
		"\u00b9\5\34\17\2\u00b9\u00ba\7\31\2\2\u00ba\u00bc\3\2\2\2\u00bb\u00ae"+
		"\3\2\2\2\u00bb\u00b0\3\2\2\2\u00bb\u00b5\3\2\2\2\u00bc\33\3\2\2\2\u00bd"+
		"\u00be\t\5\2\2\u00be\35\3\2\2\2\23\'\65<LOWbinsx|\u0081\u008a\u009d\u00ac"+
		"\u00bb";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}