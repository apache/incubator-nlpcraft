// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, SQSTRING=6, DQSTRING=7, PRED_OP=8, 
		AND=9, OR=10, VERT=11, EXCL=12, LPAREN=13, RPAREN=14, LCURLY=15, RCURLY=16, 
		SQUOTE=17, DQUOTE=18, TILDA=19, RIGHT=20, LBR=21, RBR=22, POUND=23, COMMA=24, 
		COLON=25, MINUS=26, DOT=27, UNDERSCORE=28, EQ=29, PLUS=30, QUESTION=31, 
		STAR=32, FSLASH=33, PERCENT=34, DOLLAR=35, POWER=36, BOOL=37, NULL=38, 
		INT=39, REAL=40, EXP=41, ID=42, WS=43, ErrorCharacter=44;
	public static final int
		RULE_intent = 0, RULE_intentId = 1, RULE_orderedDecl = 2, RULE_flowDecl = 3, 
		RULE_metaDecl = 4, RULE_jsonObj = 5, RULE_jsonPair = 6, RULE_jsonVal = 7, 
		RULE_jsonArr = 8, RULE_terms = 9, RULE_termEq = 10, RULE_term = 11, RULE_clsNer = 12, 
		RULE_javaFqn = 13, RULE_termId = 14, RULE_termDef = 15, RULE_termPred = 16, 
		RULE_expr = 17, RULE_val = 18, RULE_qstring = 19, RULE_minMax = 20, RULE_minMaxShortcut = 21, 
		RULE_minMaxRange = 22;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "intentId", "orderedDecl", "flowDecl", "metaDecl", "jsonObj", 
			"jsonPair", "jsonVal", "jsonArr", "terms", "termEq", "term", "clsNer", 
			"javaFqn", "termId", "termDef", "termPred", "expr", "val", "qstring", 
			"minMax", "minMaxShortcut", "minMaxRange"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'term'", null, null, 
			null, "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", "'''", 
			"'\"'", "'~'", "'>>'", "'['", "']'", "'#'", "','", "':'", "'-'", "'.'", 
			"'_'", "'='", "'+'", "'?'", "'*'", "'/'", "'%'", "'$'", "'^'", null, 
			"'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "SQSTRING", "DQSTRING", "PRED_OP", 
			"AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", "LCURLY", "RCURLY", 
			"SQUOTE", "DQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "POUND", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"FSLASH", "PERCENT", "DOLLAR", "POWER", "BOOL", "NULL", "INT", "REAL", 
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
	public String getGrammarFileName() { return "NCIntentDsl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCIntentDslParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class IntentContext extends ParserRuleContext {
		public IntentIdContext intentId() {
			return getRuleContext(IntentIdContext.class,0);
		}
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TerminalNode EOF() { return getToken(NCIntentDslParser.EOF, 0); }
		public OrderedDeclContext orderedDecl() {
			return getRuleContext(OrderedDeclContext.class,0);
		}
		public FlowDeclContext flowDecl() {
			return getRuleContext(FlowDeclContext.class,0);
		}
		public MetaDeclContext metaDecl() {
			return getRuleContext(MetaDeclContext.class,0);
		}
		public IntentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intent; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIntent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIntent(this);
		}
	}

	public final IntentContext intent() throws RecognitionException {
		IntentContext _localctx = new IntentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_intent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(46);
			intentId();
			setState(48);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(47);
				orderedDecl();
				}
			}

			setState(51);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(50);
				flowDecl();
				}
			}

			setState(54);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(53);
				metaDecl();
				}
			}

			setState(56);
			terms(0);
			setState(57);
			match(EOF);
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

	public static class IntentIdContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public IntentIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intentId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIntentId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIntentId(this);
		}
	}

	public final IntentIdContext intentId() throws RecognitionException {
		IntentIdContext _localctx = new IntentIdContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_intentId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(59);
			match(T__0);
			setState(60);
			match(EQ);
			setState(61);
			match(ID);
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

	public static class OrderedDeclContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public OrderedDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterOrderedDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitOrderedDecl(this);
		}
	}

	public final OrderedDeclContext orderedDecl() throws RecognitionException {
		OrderedDeclContext _localctx = new OrderedDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_orderedDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			match(T__1);
			setState(64);
			match(EQ);
			setState(65);
			match(BOOL);
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

	public static class FlowDeclContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public FlowDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlowDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlowDecl(this);
		}
	}

	public final FlowDeclContext flowDecl() throws RecognitionException {
		FlowDeclContext _localctx = new FlowDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_flowDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			match(T__2);
			setState(68);
			match(EQ);
			setState(69);
			qstring();
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

	public static class MetaDeclContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public MetaDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMetaDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMetaDecl(this);
		}
	}

	public final MetaDeclContext metaDecl() throws RecognitionException {
		MetaDeclContext _localctx = new MetaDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_metaDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(T__3);
			setState(72);
			match(EQ);
			setState(73);
			jsonObj();
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

	public static class JsonObjContext extends ParserRuleContext {
		public TerminalNode LCURLY() { return getToken(NCIntentDslParser.LCURLY, 0); }
		public List<JsonPairContext> jsonPair() {
			return getRuleContexts(JsonPairContext.class);
		}
		public JsonPairContext jsonPair(int i) {
			return getRuleContext(JsonPairContext.class,i);
		}
		public TerminalNode RCURLY() { return getToken(NCIntentDslParser.RCURLY, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NCIntentDslParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NCIntentDslParser.COMMA, i);
		}
		public JsonObjContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonObj; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterJsonObj(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitJsonObj(this);
		}
	}

	public final JsonObjContext jsonObj() throws RecognitionException {
		JsonObjContext _localctx = new JsonObjContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_jsonObj);
		int _la;
		try {
			setState(88);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(75);
				match(LCURLY);
				setState(76);
				jsonPair();
				setState(81);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(77);
					match(COMMA);
					setState(78);
					jsonPair();
					}
					}
					setState(83);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(84);
				match(RCURLY);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
				match(LCURLY);
				setState(87);
				match(RCURLY);
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

	public static class JsonPairContext extends ParserRuleContext {
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TerminalNode COLON() { return getToken(NCIntentDslParser.COLON, 0); }
		public JsonValContext jsonVal() {
			return getRuleContext(JsonValContext.class,0);
		}
		public JsonPairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonPair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterJsonPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitJsonPair(this);
		}
	}

	public final JsonPairContext jsonPair() throws RecognitionException {
		JsonPairContext _localctx = new JsonPairContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_jsonPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(90);
			qstring();
			setState(91);
			match(COLON);
			setState(92);
			jsonVal();
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

	public static class JsonValContext extends ParserRuleContext {
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode REAL() { return getToken(NCIntentDslParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIntentDslParser.EXP, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public JsonArrContext jsonArr() {
			return getRuleContext(JsonArrContext.class,0);
		}
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public TerminalNode NULL() { return getToken(NCIntentDslParser.NULL, 0); }
		public JsonValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterJsonVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitJsonVal(this);
		}
	}

	public final JsonValContext jsonVal() throws RecognitionException {
		JsonValContext _localctx = new JsonValContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_jsonVal);
		int _la;
		try {
			setState(109);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(94);
				qstring();
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(95);
					match(MINUS);
					}
				}

				setState(98);
				match(INT);
				setState(100);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REAL) {
					{
					setState(99);
					match(REAL);
					}
				}

				setState(103);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXP) {
					{
					setState(102);
					match(EXP);
					}
				}

				}
				break;
			case LCURLY:
				enterOuterAlt(_localctx, 3);
				{
				setState(105);
				jsonObj();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 4);
				{
				setState(106);
				jsonArr();
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 5);
				{
				setState(107);
				match(BOOL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(108);
				match(NULL);
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

	public static class JsonArrContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public List<JsonValContext> jsonVal() {
			return getRuleContexts(JsonValContext.class);
		}
		public JsonValContext jsonVal(int i) {
			return getRuleContext(JsonValContext.class,i);
		}
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NCIntentDslParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NCIntentDslParser.COMMA, i);
		}
		public JsonArrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonArr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterJsonArr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitJsonArr(this);
		}
	}

	public final JsonArrContext jsonArr() throws RecognitionException {
		JsonArrContext _localctx = new JsonArrContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_jsonArr);
		int _la;
		try {
			setState(124);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(111);
				match(LBR);
				setState(112);
				jsonVal();
				setState(117);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(113);
					match(COMMA);
					setState(114);
					jsonVal();
					}
					}
					setState(119);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(120);
				match(RBR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(122);
				match(LBR);
				setState(123);
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

	public static class TermsContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TermsContext terms() {
			return getRuleContext(TermsContext.class,0);
		}
		public TermsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_terms; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTerms(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTerms(this);
		}
	}

	public final TermsContext terms() throws RecognitionException {
		return terms(0);
	}

	private TermsContext terms(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermsContext _localctx = new TermsContext(_ctx, _parentState);
		TermsContext _prevctx = _localctx;
		int _startState = 18;
		enterRecursionRule(_localctx, 18, RULE_terms, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(127);
			term();
			}
			_ctx.stop = _input.LT(-1);
			setState(133);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_terms);
					setState(129);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(130);
					term();
					}
					} 
				}
				setState(135);
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

	public static class TermEqContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode TILDA() { return getToken(NCIntentDslParser.TILDA, 0); }
		public TermEqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termEq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTermEq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTermEq(this);
		}
	}

	public final TermEqContext termEq() throws RecognitionException {
		TermEqContext _localctx = new TermEqContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			_la = _input.LA(1);
			if ( !(_la==TILDA || _la==EQ) ) {
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

	public static class TermContext extends ParserRuleContext {
		public TermEqContext termEq() {
			return getRuleContext(TermEqContext.class,0);
		}
		public TermIdContext termId() {
			return getRuleContext(TermIdContext.class,0);
		}
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public TerminalNode LCURLY() { return getToken(NCIntentDslParser.LCURLY, 0); }
		public TermDefContext termDef() {
			return getRuleContext(TermDefContext.class,0);
		}
		public TerminalNode RCURLY() { return getToken(NCIntentDslParser.RCURLY, 0); }
		public List<TerminalNode> FSLASH() { return getTokens(NCIntentDslParser.FSLASH); }
		public TerminalNode FSLASH(int i) {
			return getToken(NCIntentDslParser.FSLASH, i);
		}
		public ClsNerContext clsNer() {
			return getRuleContext(ClsNerContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(T__4);
			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(139);
				termId();
				}
			}

			setState(142);
			termEq();
			setState(151);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LCURLY:
				{
				{
				setState(143);
				match(LCURLY);
				setState(144);
				termDef(0);
				setState(145);
				match(RCURLY);
				}
				}
				break;
			case FSLASH:
				{
				{
				setState(147);
				match(FSLASH);
				setState(148);
				clsNer();
				setState(149);
				match(FSLASH);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(154);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(153);
				minMax();
				}
				break;
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

	public static class ClsNerContext extends ParserRuleContext {
		public TerminalNode POUND() { return getToken(NCIntentDslParser.POUND, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public JavaFqnContext javaFqn() {
			return getRuleContext(JavaFqnContext.class,0);
		}
		public ClsNerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_clsNer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterClsNer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitClsNer(this);
		}
	}

	public final ClsNerContext clsNer() throws RecognitionException {
		ClsNerContext _localctx = new ClsNerContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_clsNer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(157);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(156);
				javaFqn(0);
				}
			}

			setState(159);
			match(POUND);
			setState(160);
			match(ID);
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

	public static class JavaFqnContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public JavaFqnContext javaFqn() {
			return getRuleContext(JavaFqnContext.class,0);
		}
		public TerminalNode DOT() { return getToken(NCIntentDslParser.DOT, 0); }
		public JavaFqnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_javaFqn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterJavaFqn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitJavaFqn(this);
		}
	}

	public final JavaFqnContext javaFqn() throws RecognitionException {
		return javaFqn(0);
	}

	private JavaFqnContext javaFqn(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		JavaFqnContext _localctx = new JavaFqnContext(_ctx, _parentState);
		JavaFqnContext _prevctx = _localctx;
		int _startState = 26;
		enterRecursionRule(_localctx, 26, RULE_javaFqn, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(163);
			match(ID);
			}
			_ctx.stop = _input.LT(-1);
			setState(170);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new JavaFqnContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_javaFqn);
					setState(165);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(166);
					match(DOT);
					setState(167);
					match(ID);
					}
					} 
				}
				setState(172);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
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

	public static class TermIdContext extends ParserRuleContext {
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TermIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTermId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTermId(this);
		}
	}

	public final TermIdContext termId() throws RecognitionException {
		TermIdContext _localctx = new TermIdContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173);
			match(LPAREN);
			setState(174);
			match(ID);
			setState(175);
			match(RPAREN);
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

	public static class TermDefContext extends ParserRuleContext {
		public TermPredContext termPred() {
			return getRuleContext(TermPredContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public List<TermDefContext> termDef() {
			return getRuleContexts(TermDefContext.class);
		}
		public TermDefContext termDef(int i) {
			return getRuleContext(TermDefContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TerminalNode EXCL() { return getToken(NCIntentDslParser.EXCL, 0); }
		public TerminalNode AND() { return getToken(NCIntentDslParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCIntentDslParser.OR, 0); }
		public TermDefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termDef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTermDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTermDef(this);
		}
	}

	public final TermDefContext termDef() throws RecognitionException {
		return termDef(0);
	}

	private TermDefContext termDef(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermDefContext _localctx = new TermDefContext(_ctx, _parentState);
		TermDefContext _prevctx = _localctx;
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_termDef, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(185);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(178);
				termPred();
				}
				break;
			case 2:
				{
				setState(179);
				match(LPAREN);
				setState(180);
				termDef(0);
				setState(181);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(183);
				match(EXCL);
				setState(184);
				termDef(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermDefContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_termDef);
					setState(187);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(188);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(189);
					termDef(3);
					}
					} 
				}
				setState(194);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
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

	public static class TermPredContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode PRED_OP() { return getToken(NCIntentDslParser.PRED_OP, 0); }
		public TermPredContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termPred; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTermPred(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTermPred(this);
		}
	}

	public final TermPredContext termPred() throws RecognitionException {
		TermPredContext _localctx = new TermPredContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_termPred);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(195);
			expr(0);
			setState(196);
			match(PRED_OP);
			setState(197);
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
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(NCIntentDslParser.PLUS, 0); }
		public TerminalNode STAR() { return getToken(NCIntentDslParser.STAR, 0); }
		public TerminalNode FSLASH() { return getToken(NCIntentDslParser.FSLASH, 0); }
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitExpr(this);
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
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(211);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
			case MINUS:
			case BOOL:
			case NULL:
			case INT:
				{
				setState(200);
				val();
				}
				break;
			case LPAREN:
				{
				setState(201);
				match(LPAREN);
				setState(202);
				expr(0);
				setState(203);
				match(RPAREN);
				}
				break;
			case ID:
				{
				setState(205);
				match(ID);
				setState(206);
				match(LPAREN);
				setState(208);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SQSTRING) | (1L << DQSTRING) | (1L << LPAREN) | (1L << MINUS) | (1L << BOOL) | (1L << NULL) | (1L << INT) | (1L << ID))) != 0)) {
					{
					setState(207);
					expr(0);
					}
				}

				setState(210);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(221);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(219);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
					case 1:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(213);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(214);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR) | (1L << FSLASH))) != 0)) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(215);
						expr(3);
						}
						break;
					case 2:
						{
						_localctx = new ExprContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(216);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(217);
						match(COMMA);
						setState(218);
						val();
						}
						break;
					}
					} 
				}
				setState(223);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
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

	public static class ValContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(NCIntentDslParser.NULL, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode REAL() { return getToken(NCIntentDslParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIntentDslParser.EXP, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public ValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_val; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitVal(this);
		}
	}

	public final ValContext val() throws RecognitionException {
		ValContext _localctx = new ValContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_val);
		int _la;
		try {
			setState(237);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(224);
				match(NULL);
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(226);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(225);
					match(MINUS);
					}
				}

				setState(228);
				match(INT);
				setState(230);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
				case 1:
					{
					setState(229);
					match(REAL);
					}
					break;
				}
				setState(233);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
				case 1:
					{
					setState(232);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(235);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(236);
				qstring();
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

	public static class QstringContext extends ParserRuleContext {
		public TerminalNode SQSTRING() { return getToken(NCIntentDslParser.SQSTRING, 0); }
		public TerminalNode DQSTRING() { return getToken(NCIntentDslParser.DQSTRING, 0); }
		public QstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterQstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitQstring(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(239);
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

	public static class MinMaxContext extends ParserRuleContext {
		public MinMaxShortcutContext minMaxShortcut() {
			return getRuleContext(MinMaxShortcutContext.class,0);
		}
		public MinMaxRangeContext minMaxRange() {
			return getRuleContext(MinMaxRangeContext.class,0);
		}
		public MinMaxContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMax; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMinMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMinMax(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_minMax);
		try {
			setState(243);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(241);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(242);
				minMaxRange();
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

	public static class MinMaxShortcutContext extends ParserRuleContext {
		public TerminalNode PLUS() { return getToken(NCIntentDslParser.PLUS, 0); }
		public TerminalNode QUESTION() { return getToken(NCIntentDslParser.QUESTION, 0); }
		public TerminalNode STAR() { return getToken(NCIntentDslParser.STAR, 0); }
		public MinMaxShortcutContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxShortcut; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMinMaxShortcut(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMinMaxShortcut(this);
		}
	}

	public final MinMaxShortcutContext minMaxShortcut() throws RecognitionException {
		MinMaxShortcutContext _localctx = new MinMaxShortcutContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(245);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << QUESTION) | (1L << STAR))) != 0)) ) {
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

	public static class MinMaxRangeContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public List<TerminalNode> INT() { return getTokens(NCIntentDslParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(NCIntentDslParser.INT, i);
		}
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public MinMaxRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMinMaxRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMinMaxRange(this);
		}
	}

	public final MinMaxRangeContext minMaxRange() throws RecognitionException {
		MinMaxRangeContext _localctx = new MinMaxRangeContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(LBR);
			setState(248);
			match(INT);
			setState(249);
			match(COMMA);
			setState(250);
			match(INT);
			setState(251);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 9:
			return terms_sempred((TermsContext)_localctx, predIndex);
		case 13:
			return javaFqn_sempred((JavaFqnContext)_localctx, predIndex);
		case 15:
			return termDef_sempred((TermDefContext)_localctx, predIndex);
		case 17:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean terms_sempred(TermsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean javaFqn_sempred(JavaFqnContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean termDef_sempred(TermDefContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 2);
		case 4:
			return precpred(_ctx, 4);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3.\u0100\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2\3\2\5"+
		"\2\63\n\2\3\2\5\2\66\n\2\3\2\5\29\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4"+
		"\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\7\7R\n\7"+
		"\f\7\16\7U\13\7\3\7\3\7\3\7\3\7\5\7[\n\7\3\b\3\b\3\b\3\b\3\t\3\t\5\tc"+
		"\n\t\3\t\3\t\5\tg\n\t\3\t\5\tj\n\t\3\t\3\t\3\t\3\t\5\tp\n\t\3\n\3\n\3"+
		"\n\3\n\7\nv\n\n\f\n\16\ny\13\n\3\n\3\n\3\n\3\n\5\n\177\n\n\3\13\3\13\3"+
		"\13\3\13\3\13\7\13\u0086\n\13\f\13\16\13\u0089\13\13\3\f\3\f\3\r\3\r\5"+
		"\r\u008f\n\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u009a\n\r\3\r\5\r"+
		"\u009d\n\r\3\16\5\16\u00a0\n\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\7\17\u00ab\n\17\f\17\16\17\u00ae\13\17\3\20\3\20\3\20\3\20\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\5\21\u00bc\n\21\3\21\3\21\3\21\7\21"+
		"\u00c1\n\21\f\21\16\21\u00c4\13\21\3\22\3\22\3\22\3\22\3\23\3\23\3\23"+
		"\3\23\3\23\3\23\3\23\3\23\3\23\5\23\u00d3\n\23\3\23\5\23\u00d6\n\23\3"+
		"\23\3\23\3\23\3\23\3\23\3\23\7\23\u00de\n\23\f\23\16\23\u00e1\13\23\3"+
		"\24\3\24\5\24\u00e5\n\24\3\24\3\24\5\24\u00e9\n\24\3\24\5\24\u00ec\n\24"+
		"\3\24\3\24\5\24\u00f0\n\24\3\25\3\25\3\26\3\26\5\26\u00f6\n\26\3\27\3"+
		"\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\2\6\24\34 $\31\2\4\6\b\n\f\16\20"+
		"\22\24\26\30\32\34\36 \"$&(*,.\2\7\4\2\25\25\37\37\3\2\13\f\5\2\34\34"+
		"  \"#\3\2\b\t\3\2 \"\2\u010c\2\60\3\2\2\2\4=\3\2\2\2\6A\3\2\2\2\bE\3\2"+
		"\2\2\nI\3\2\2\2\fZ\3\2\2\2\16\\\3\2\2\2\20o\3\2\2\2\22~\3\2\2\2\24\u0080"+
		"\3\2\2\2\26\u008a\3\2\2\2\30\u008c\3\2\2\2\32\u009f\3\2\2\2\34\u00a4\3"+
		"\2\2\2\36\u00af\3\2\2\2 \u00bb\3\2\2\2\"\u00c5\3\2\2\2$\u00d5\3\2\2\2"+
		"&\u00ef\3\2\2\2(\u00f1\3\2\2\2*\u00f5\3\2\2\2,\u00f7\3\2\2\2.\u00f9\3"+
		"\2\2\2\60\62\5\4\3\2\61\63\5\6\4\2\62\61\3\2\2\2\62\63\3\2\2\2\63\65\3"+
		"\2\2\2\64\66\5\b\5\2\65\64\3\2\2\2\65\66\3\2\2\2\668\3\2\2\2\679\5\n\6"+
		"\28\67\3\2\2\289\3\2\2\29:\3\2\2\2:;\5\24\13\2;<\7\2\2\3<\3\3\2\2\2=>"+
		"\7\3\2\2>?\7\37\2\2?@\7,\2\2@\5\3\2\2\2AB\7\4\2\2BC\7\37\2\2CD\7\'\2\2"+
		"D\7\3\2\2\2EF\7\5\2\2FG\7\37\2\2GH\5(\25\2H\t\3\2\2\2IJ\7\6\2\2JK\7\37"+
		"\2\2KL\5\f\7\2L\13\3\2\2\2MN\7\21\2\2NS\5\16\b\2OP\7\32\2\2PR\5\16\b\2"+
		"QO\3\2\2\2RU\3\2\2\2SQ\3\2\2\2ST\3\2\2\2TV\3\2\2\2US\3\2\2\2VW\7\22\2"+
		"\2W[\3\2\2\2XY\7\21\2\2Y[\7\22\2\2ZM\3\2\2\2ZX\3\2\2\2[\r\3\2\2\2\\]\5"+
		"(\25\2]^\7\33\2\2^_\5\20\t\2_\17\3\2\2\2`p\5(\25\2ac\7\34\2\2ba\3\2\2"+
		"\2bc\3\2\2\2cd\3\2\2\2df\7)\2\2eg\7*\2\2fe\3\2\2\2fg\3\2\2\2gi\3\2\2\2"+
		"hj\7+\2\2ih\3\2\2\2ij\3\2\2\2jp\3\2\2\2kp\5\f\7\2lp\5\22\n\2mp\7\'\2\2"+
		"np\7(\2\2o`\3\2\2\2ob\3\2\2\2ok\3\2\2\2ol\3\2\2\2om\3\2\2\2on\3\2\2\2"+
		"p\21\3\2\2\2qr\7\27\2\2rw\5\20\t\2st\7\32\2\2tv\5\20\t\2us\3\2\2\2vy\3"+
		"\2\2\2wu\3\2\2\2wx\3\2\2\2xz\3\2\2\2yw\3\2\2\2z{\7\30\2\2{\177\3\2\2\2"+
		"|}\7\27\2\2}\177\7\30\2\2~q\3\2\2\2~|\3\2\2\2\177\23\3\2\2\2\u0080\u0081"+
		"\b\13\1\2\u0081\u0082\5\30\r\2\u0082\u0087\3\2\2\2\u0083\u0084\f\3\2\2"+
		"\u0084\u0086\5\30\r\2\u0085\u0083\3\2\2\2\u0086\u0089\3\2\2\2\u0087\u0085"+
		"\3\2\2\2\u0087\u0088\3\2\2\2\u0088\25\3\2\2\2\u0089\u0087\3\2\2\2\u008a"+
		"\u008b\t\2\2\2\u008b\27\3\2\2\2\u008c\u008e\7\7\2\2\u008d\u008f\5\36\20"+
		"\2\u008e\u008d\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0099"+
		"\5\26\f\2\u0091\u0092\7\21\2\2\u0092\u0093\5 \21\2\u0093\u0094\7\22\2"+
		"\2\u0094\u009a\3\2\2\2\u0095\u0096\7#\2\2\u0096\u0097\5\32\16\2\u0097"+
		"\u0098\7#\2\2\u0098\u009a\3\2\2\2\u0099\u0091\3\2\2\2\u0099\u0095\3\2"+
		"\2\2\u009a\u009c\3\2\2\2\u009b\u009d\5*\26\2\u009c\u009b\3\2\2\2\u009c"+
		"\u009d\3\2\2\2\u009d\31\3\2\2\2\u009e\u00a0\5\34\17\2\u009f\u009e\3\2"+
		"\2\2\u009f\u00a0\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1\u00a2\7\31\2\2\u00a2"+
		"\u00a3\7,\2\2\u00a3\33\3\2\2\2\u00a4\u00a5\b\17\1\2\u00a5\u00a6\7,\2\2"+
		"\u00a6\u00ac\3\2\2\2\u00a7\u00a8\f\3\2\2\u00a8\u00a9\7\35\2\2\u00a9\u00ab"+
		"\7,\2\2\u00aa\u00a7\3\2\2\2\u00ab\u00ae\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ac"+
		"\u00ad\3\2\2\2\u00ad\35\3\2\2\2\u00ae\u00ac\3\2\2\2\u00af\u00b0\7\17\2"+
		"\2\u00b0\u00b1\7,\2\2\u00b1\u00b2\7\20\2\2\u00b2\37\3\2\2\2\u00b3\u00b4"+
		"\b\21\1\2\u00b4\u00bc\5\"\22\2\u00b5\u00b6\7\17\2\2\u00b6\u00b7\5 \21"+
		"\2\u00b7\u00b8\7\20\2\2\u00b8\u00bc\3\2\2\2\u00b9\u00ba\7\16\2\2\u00ba"+
		"\u00bc\5 \21\3\u00bb\u00b3\3\2\2\2\u00bb\u00b5\3\2\2\2\u00bb\u00b9\3\2"+
		"\2\2\u00bc\u00c2\3\2\2\2\u00bd\u00be\f\4\2\2\u00be\u00bf\t\3\2\2\u00bf"+
		"\u00c1\5 \21\5\u00c0\u00bd\3\2\2\2\u00c1\u00c4\3\2\2\2\u00c2\u00c0\3\2"+
		"\2\2\u00c2\u00c3\3\2\2\2\u00c3!\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c5\u00c6"+
		"\5$\23\2\u00c6\u00c7\7\n\2\2\u00c7\u00c8\5$\23\2\u00c8#\3\2\2\2\u00c9"+
		"\u00ca\b\23\1\2\u00ca\u00d6\5&\24\2\u00cb\u00cc\7\17\2\2\u00cc\u00cd\5"+
		"$\23\2\u00cd\u00ce\7\20\2\2\u00ce\u00d6\3\2\2\2\u00cf\u00d0\7,\2\2\u00d0"+
		"\u00d2\7\17\2\2\u00d1\u00d3\5$\23\2\u00d2\u00d1\3\2\2\2\u00d2\u00d3\3"+
		"\2\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d6\7\20\2\2\u00d5\u00c9\3\2\2\2\u00d5"+
		"\u00cb\3\2\2\2\u00d5\u00cf\3\2\2\2\u00d6\u00df\3\2\2\2\u00d7\u00d8\f\4"+
		"\2\2\u00d8\u00d9\t\4\2\2\u00d9\u00de\5$\23\5\u00da\u00db\f\6\2\2\u00db"+
		"\u00dc\7\32\2\2\u00dc\u00de\5&\24\2\u00dd\u00d7\3\2\2\2\u00dd\u00da\3"+
		"\2\2\2\u00de\u00e1\3\2\2\2\u00df\u00dd\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0"+
		"%\3\2\2\2\u00e1\u00df\3\2\2\2\u00e2\u00f0\7(\2\2\u00e3\u00e5\7\34\2\2"+
		"\u00e4\u00e3\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e8"+
		"\7)\2\2\u00e7\u00e9\7*\2\2\u00e8\u00e7\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9"+
		"\u00eb\3\2\2\2\u00ea\u00ec\7+\2\2\u00eb\u00ea\3\2\2\2\u00eb\u00ec\3\2"+
		"\2\2\u00ec\u00f0\3\2\2\2\u00ed\u00f0\7\'\2\2\u00ee\u00f0\5(\25\2\u00ef"+
		"\u00e2\3\2\2\2\u00ef\u00e4\3\2\2\2\u00ef\u00ed\3\2\2\2\u00ef\u00ee\3\2"+
		"\2\2\u00f0\'\3\2\2\2\u00f1\u00f2\t\5\2\2\u00f2)\3\2\2\2\u00f3\u00f6\5"+
		",\27\2\u00f4\u00f6\5.\30\2\u00f5\u00f3\3\2\2\2\u00f5\u00f4\3\2\2\2\u00f6"+
		"+\3\2\2\2\u00f7\u00f8\t\6\2\2\u00f8-\3\2\2\2\u00f9\u00fa\7\27\2\2\u00fa"+
		"\u00fb\7)\2\2\u00fb\u00fc\7\32\2\2\u00fc\u00fd\7)\2\2\u00fd\u00fe\7\30"+
		"\2\2\u00fe/\3\2\2\2\36\62\658SZbfiow~\u0087\u008e\u0099\u009c\u009f\u00ac"+
		"\u00bb\u00c2\u00d2\u00d5\u00dd\u00df\u00e4\u00e8\u00eb\u00ef\u00f5";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}