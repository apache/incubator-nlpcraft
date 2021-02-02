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
		COMMA=24, COLON=25, POUND=26, MINUS=27, DOT=28, UNDERSCORE=29, BOOL=30, 
		INT=31, EXP=32, ID=33, WS=34, ErrorCharacter=35;
	public static final int
		RULE_synonym = 0, RULE_alias = 1, RULE_item = 2, RULE_pred = 3, RULE_expr = 4, 
		RULE_val = 5, RULE_singleVal = 6, RULE_tokQual = 7, RULE_tokQualPart = 8, 
		RULE_tokMeta = 9, RULE_modelMeta = 10, RULE_intentMeta = 11, RULE_qstring = 12;
	private static String[] makeRuleNames() {
		return new String[] {
			"synonym", "alias", "item", "pred", "expr", "val", "singleVal", "tokQual", 
			"tokQualPart", "tokMeta", "modelMeta", "intentMeta", "qstring"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'null'", "'id'", "'aliases'", "'startidx'", "'endidx'", "'parent'", 
			"'groups'", "'ancestors'", "'value'", null, null, null, "'&&'", "'||'", 
			"'!'", "'('", "')'", "'''", "'\"'", "'$'", "'~'", "'['", "']'", "','", 
			"':'", "'#'", "'-'", "'.'", "'_'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, "SQSTRING", 
			"DQSTRING", "PRED_OP", "AND", "OR", "EXCL", "LPAREN", "RPAREN", "SQUOTE", 
			"DQUOTE", "DOLLAR", "TILDA", "LBR", "RBR", "COMMA", "COLON", "POUND", 
			"MINUS", "DOT", "UNDERSCORE", "BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
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
			setState(35);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBR:
				enterOuterAlt(_localctx, 1);
				{
				setState(26);
				alias();
				setState(27);
				match(LPAREN);
				setState(28);
				item(0);
				setState(29);
				match(RPAREN);
				setState(30);
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
				setState(32);
				item(0);
				setState(33);
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
			setState(37);
			match(LBR);
			setState(38);
			match(ID);
			setState(39);
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
			setState(49);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(42);
				pred();
				}
				break;
			case 2:
				{
				setState(43);
				match(LPAREN);
				setState(44);
				item(0);
				setState(45);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(47);
				match(EXCL);
				setState(48);
				item(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(56);
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
					setState(51);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(52);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(53);
					item(3);
					}
					} 
				}
				setState(58);
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
			setState(59);
			expr();
			setState(60);
			match(PRED_OP);
			setState(61);
			expr();
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
		public TerminalNode ID() { return getToken(NCSynonymDslParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(NCSynonymDslParser.LPAREN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCSynonymDslParser.RPAREN, 0); }
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
		ExprContext _localctx = new ExprContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_expr);
		try {
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(63);
				val(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(64);
				match(ID);
				setState(65);
				match(LPAREN);
				setState(66);
				expr();
				setState(67);
				match(RPAREN);
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
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_val, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
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
				setState(72);
				singleVal();
				}
				break;
			case LPAREN:
				{
				setState(73);
				match(LPAREN);
				setState(74);
				val(0);
				setState(75);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(84);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ValContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_val);
					setState(79);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(80);
					match(COMMA);
					setState(81);
					val(2);
					}
					} 
				}
				setState(86);
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
		enterRule(_localctx, 12, RULE_singleVal);
		int _la;
		try {
			setState(108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(87);
				match(T__0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(89);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(88);
					match(MINUS);
					}
				}

				setState(94);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
				case 1:
					{
					setState(91);
					match(INT);
					}
					break;
				case 2:
					{
					setState(92);
					match(INT);
					setState(93);
					match(EXP);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(96);
				match(BOOL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(97);
				qstring();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(98);
					tokQual(0);
					}
				}

				setState(101);
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
				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(102);
					tokQual(0);
					}
				}

				setState(105);
				tokMeta();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(106);
				modelMeta();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(107);
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
		int _startState = 14;
		enterRecursionRule(_localctx, 14, RULE_tokQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(111);
			tokQualPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(117);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TokQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_tokQual);
					setState(113);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(114);
					tokQualPart();
					}
					} 
				}
				setState(119);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
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
		enterRule(_localctx, 16, RULE_tokQualPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(120);
			match(ID);
			setState(121);
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
		enterRule(_localctx, 18, RULE_tokMeta);
		try {
			setState(136);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(123);
				match(TILDA);
				setState(124);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
				match(TILDA);
				setState(126);
				match(ID);
				setState(127);
				match(LBR);
				setState(128);
				match(INT);
				setState(129);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(130);
				match(TILDA);
				setState(131);
				match(ID);
				setState(132);
				match(LBR);
				setState(133);
				qstring();
				setState(134);
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
		enterRule(_localctx, 20, RULE_modelMeta);
		try {
			setState(151);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(138);
				match(POUND);
				setState(139);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(140);
				match(POUND);
				setState(141);
				match(ID);
				setState(142);
				match(LBR);
				setState(143);
				match(INT);
				setState(144);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(145);
				match(POUND);
				setState(146);
				match(ID);
				setState(147);
				match(LBR);
				setState(148);
				qstring();
				setState(149);
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
		enterRule(_localctx, 22, RULE_intentMeta);
		try {
			setState(166);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(153);
				match(DOLLAR);
				setState(154);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(155);
				match(DOLLAR);
				setState(156);
				match(ID);
				setState(157);
				match(LBR);
				setState(158);
				match(INT);
				setState(159);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(160);
				match(DOLLAR);
				setState(161);
				match(ID);
				setState(162);
				match(LBR);
				setState(163);
				qstring();
				setState(164);
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
		enterRule(_localctx, 24, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
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
		case 5:
			return val_sempred((ValContext)_localctx, predIndex);
		case 7:
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
	private boolean val_sempred(ValContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean tokQual_sempred(TokQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3%\u00ad\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2"+
		"&\n\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\64\n\4\3\4\3"+
		"\4\3\4\7\49\n\4\f\4\16\4<\13\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\5\6H\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7P\n\7\3\7\3\7\3\7\7\7U\n\7\f\7\16"+
		"\7X\13\7\3\b\3\b\5\b\\\n\b\3\b\3\b\3\b\5\ba\n\b\3\b\3\b\3\b\5\bf\n\b\3"+
		"\b\3\b\5\bj\n\b\3\b\3\b\3\b\5\bo\n\b\3\t\3\t\3\t\3\t\3\t\7\tv\n\t\f\t"+
		"\16\ty\13\t\3\n\3\n\3\n\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3"+
		"\13\3\13\3\13\3\13\5\13\u008b\n\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\5\f\u009a\n\f\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\5\r\u00a9\n\r\3\16\3\16\3\16\2\5\6\f\20\17\2\4\6\b\n\f\16"+
		"\20\22\24\26\30\32\2\5\3\2\17\20\3\2\4\13\3\2\f\r\2\u00b8\2%\3\2\2\2\4"+
		"\'\3\2\2\2\6\63\3\2\2\2\b=\3\2\2\2\nG\3\2\2\2\fO\3\2\2\2\16n\3\2\2\2\20"+
		"p\3\2\2\2\22z\3\2\2\2\24\u008a\3\2\2\2\26\u0099\3\2\2\2\30\u00a8\3\2\2"+
		"\2\32\u00aa\3\2\2\2\34\35\5\4\3\2\35\36\7\22\2\2\36\37\5\6\4\2\37 \7\23"+
		"\2\2 !\7\2\2\3!&\3\2\2\2\"#\5\6\4\2#$\7\2\2\3$&\3\2\2\2%\34\3\2\2\2%\""+
		"\3\2\2\2&\3\3\2\2\2\'(\7\30\2\2()\7#\2\2)*\7\31\2\2*\5\3\2\2\2+,\b\4\1"+
		"\2,\64\5\b\5\2-.\7\22\2\2./\5\6\4\2/\60\7\23\2\2\60\64\3\2\2\2\61\62\7"+
		"\21\2\2\62\64\5\6\4\3\63+\3\2\2\2\63-\3\2\2\2\63\61\3\2\2\2\64:\3\2\2"+
		"\2\65\66\f\4\2\2\66\67\t\2\2\2\679\5\6\4\58\65\3\2\2\29<\3\2\2\2:8\3\2"+
		"\2\2:;\3\2\2\2;\7\3\2\2\2<:\3\2\2\2=>\5\n\6\2>?\7\16\2\2?@\5\n\6\2@\t"+
		"\3\2\2\2AH\5\f\7\2BC\7#\2\2CD\7\22\2\2DE\5\n\6\2EF\7\23\2\2FH\3\2\2\2"+
		"GA\3\2\2\2GB\3\2\2\2H\13\3\2\2\2IJ\b\7\1\2JP\5\16\b\2KL\7\22\2\2LM\5\f"+
		"\7\2MN\7\23\2\2NP\3\2\2\2OI\3\2\2\2OK\3\2\2\2PV\3\2\2\2QR\f\3\2\2RS\7"+
		"\32\2\2SU\5\f\7\4TQ\3\2\2\2UX\3\2\2\2VT\3\2\2\2VW\3\2\2\2W\r\3\2\2\2X"+
		"V\3\2\2\2Yo\7\3\2\2Z\\\7\35\2\2[Z\3\2\2\2[\\\3\2\2\2\\`\3\2\2\2]a\7!\2"+
		"\2^_\7!\2\2_a\7\"\2\2`]\3\2\2\2`^\3\2\2\2ao\3\2\2\2bo\7 \2\2co\5\32\16"+
		"\2df\5\20\t\2ed\3\2\2\2ef\3\2\2\2fg\3\2\2\2go\t\3\2\2hj\5\20\t\2ih\3\2"+
		"\2\2ij\3\2\2\2jk\3\2\2\2ko\5\24\13\2lo\5\26\f\2mo\5\30\r\2nY\3\2\2\2n"+
		"[\3\2\2\2nb\3\2\2\2nc\3\2\2\2ne\3\2\2\2ni\3\2\2\2nl\3\2\2\2nm\3\2\2\2"+
		"o\17\3\2\2\2pq\b\t\1\2qr\5\22\n\2rw\3\2\2\2st\f\3\2\2tv\5\22\n\2us\3\2"+
		"\2\2vy\3\2\2\2wu\3\2\2\2wx\3\2\2\2x\21\3\2\2\2yw\3\2\2\2z{\7#\2\2{|\7"+
		"\36\2\2|\23\3\2\2\2}~\7\27\2\2~\u008b\7#\2\2\177\u0080\7\27\2\2\u0080"+
		"\u0081\7#\2\2\u0081\u0082\7\30\2\2\u0082\u0083\7!\2\2\u0083\u008b\7\31"+
		"\2\2\u0084\u0085\7\27\2\2\u0085\u0086\7#\2\2\u0086\u0087\7\30\2\2\u0087"+
		"\u0088\5\32\16\2\u0088\u0089\7\31\2\2\u0089\u008b\3\2\2\2\u008a}\3\2\2"+
		"\2\u008a\177\3\2\2\2\u008a\u0084\3\2\2\2\u008b\25\3\2\2\2\u008c\u008d"+
		"\7\34\2\2\u008d\u009a\7#\2\2\u008e\u008f\7\34\2\2\u008f\u0090\7#\2\2\u0090"+
		"\u0091\7\30\2\2\u0091\u0092\7!\2\2\u0092\u009a\7\31\2\2\u0093\u0094\7"+
		"\34\2\2\u0094\u0095\7#\2\2\u0095\u0096\7\30\2\2\u0096\u0097\5\32\16\2"+
		"\u0097\u0098\7\31\2\2\u0098\u009a\3\2\2\2\u0099\u008c\3\2\2\2\u0099\u008e"+
		"\3\2\2\2\u0099\u0093\3\2\2\2\u009a\27\3\2\2\2\u009b\u009c\7\26\2\2\u009c"+
		"\u00a9\7#\2\2\u009d\u009e\7\26\2\2\u009e\u009f\7#\2\2\u009f\u00a0\7\30"+
		"\2\2\u00a0\u00a1\7!\2\2\u00a1\u00a9\7\31\2\2\u00a2\u00a3\7\26\2\2\u00a3"+
		"\u00a4\7#\2\2\u00a4\u00a5\7\30\2\2\u00a5\u00a6\5\32\16\2\u00a6\u00a7\7"+
		"\31\2\2\u00a7\u00a9\3\2\2\2\u00a8\u009b\3\2\2\2\u00a8\u009d\3\2\2\2\u00a8"+
		"\u00a2\3\2\2\2\u00a9\31\3\2\2\2\u00aa\u00ab\t\4\2\2\u00ab\33\3\2\2\2\21"+
		"%\63:GOV[`einw\u008a\u0099\u00a8";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}