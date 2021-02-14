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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, SQSTRING=6, DQSTRING=7, BOOL=8, 
		NULL=9, EQ=10, NEQ=11, GTEQ=12, LTEQ=13, GT=14, LT=15, AND=16, OR=17, 
		VERT=18, NOT=19, LPAR=20, RPAR=21, LBRACE=22, RBRACE=23, SQUOTE=24, DQUOTE=25, 
		TILDA=26, LBR=27, RBR=28, POUND=29, COMMA=30, COLON=31, MINUS=32, DOT=33, 
		UNDERSCORE=34, ASSIGN=35, PLUS=36, QUESTION=37, MULT=38, DIV=39, MOD=40, 
		DOLLAR=41, INT=42, REAL=43, EXP=44, ID=45, WS=46, ErrorCharacter=47;
	public static final int
		RULE_intent = 0, RULE_intentId = 1, RULE_orderedDecl = 2, RULE_flowDecl = 3, 
		RULE_metaDecl = 4, RULE_jsonObj = 5, RULE_jsonPair = 6, RULE_jsonVal = 7, 
		RULE_jsonArr = 8, RULE_terms = 9, RULE_termEq = 10, RULE_term = 11, RULE_clsNer = 12, 
		RULE_javaFqn = 13, RULE_termId = 14, RULE_expr = 15, RULE_atom = 16, RULE_qstring = 17, 
		RULE_minMax = 18, RULE_minMaxShortcut = 19, RULE_minMaxRange = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "intentId", "orderedDecl", "flowDecl", "metaDecl", "jsonObj", 
			"jsonPair", "jsonVal", "jsonArr", "terms", "termEq", "term", "clsNer", 
			"javaFqn", "termId", "expr", "atom", "qstring", "minMax", "minMaxShortcut", 
			"minMaxRange"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'term'", null, null, 
			null, "'null'", "'=='", "'!='", "'>='", "'<='", "'>'", "'<'", "'&&'", 
			"'||'", "'|'", "'!'", "'('", "')'", "'{'", "'}'", "'''", "'\"'", "'~'", 
			"'['", "']'", "'#'", "','", "':'", "'-'", "'.'", "'_'", "'='", "'+'", 
			"'?'", "'*'", "'/'", "'%'", "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "SQSTRING", "DQSTRING", "BOOL", "NULL", 
			"EQ", "NEQ", "GTEQ", "LTEQ", "GT", "LT", "AND", "OR", "VERT", "NOT", 
			"LPAR", "RPAR", "LBRACE", "RBRACE", "SQUOTE", "DQUOTE", "TILDA", "LBR", 
			"RBR", "POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "ASSIGN", 
			"PLUS", "QUESTION", "MULT", "DIV", "MOD", "DOLLAR", "INT", "REAL", "EXP", 
			"ID", "WS", "ErrorCharacter"
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
			setState(42);
			intentId();
			setState(44);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(43);
				orderedDecl();
				}
			}

			setState(47);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(46);
				flowDecl();
				}
			}

			setState(50);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(49);
				metaDecl();
				}
			}

			setState(52);
			terms(0);
			setState(53);
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
		public TerminalNode ASSIGN() { return getToken(NCIntentDslParser.ASSIGN, 0); }
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
			setState(55);
			match(T__0);
			setState(56);
			match(ASSIGN);
			setState(57);
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
		public TerminalNode ASSIGN() { return getToken(NCIntentDslParser.ASSIGN, 0); }
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
			setState(59);
			match(T__1);
			setState(60);
			match(ASSIGN);
			setState(61);
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
		public TerminalNode ASSIGN() { return getToken(NCIntentDslParser.ASSIGN, 0); }
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
			setState(63);
			match(T__2);
			setState(64);
			match(ASSIGN);
			setState(65);
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
		public TerminalNode ASSIGN() { return getToken(NCIntentDslParser.ASSIGN, 0); }
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
			setState(67);
			match(T__3);
			setState(68);
			match(ASSIGN);
			setState(69);
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
		public TerminalNode LBRACE() { return getToken(NCIntentDslParser.LBRACE, 0); }
		public List<JsonPairContext> jsonPair() {
			return getRuleContexts(JsonPairContext.class);
		}
		public JsonPairContext jsonPair(int i) {
			return getRuleContext(JsonPairContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(NCIntentDslParser.RBRACE, 0); }
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
			setState(84);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(71);
				match(LBRACE);
				setState(72);
				jsonPair();
				setState(77);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(73);
					match(COMMA);
					setState(74);
					jsonPair();
					}
					}
					setState(79);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(80);
				match(RBRACE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(82);
				match(LBRACE);
				setState(83);
				match(RBRACE);
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
			setState(86);
			qstring();
			setState(87);
			match(COLON);
			setState(88);
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
			setState(105);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				qstring();
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(92);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(91);
					match(MINUS);
					}
				}

				setState(94);
				match(INT);
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REAL) {
					{
					setState(95);
					match(REAL);
					}
				}

				setState(99);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXP) {
					{
					setState(98);
					match(EXP);
					}
				}

				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(101);
				jsonObj();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 4);
				{
				setState(102);
				jsonArr();
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 5);
				{
				setState(103);
				match(BOOL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(104);
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
			setState(120);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(107);
				match(LBR);
				setState(108);
				jsonVal();
				setState(113);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(109);
					match(COMMA);
					setState(110);
					jsonVal();
					}
					}
					setState(115);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(116);
				match(RBR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(118);
				match(LBR);
				setState(119);
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
			setState(123);
			term();
			}
			_ctx.stop = _input.LT(-1);
			setState(129);
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
					setState(125);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(126);
					term();
					}
					} 
				}
				setState(131);
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
		public TerminalNode ASSIGN() { return getToken(NCIntentDslParser.ASSIGN, 0); }
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
			setState(132);
			_la = _input.LA(1);
			if ( !(_la==TILDA || _la==ASSIGN) ) {
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
		public TerminalNode LBRACE() { return getToken(NCIntentDslParser.LBRACE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(NCIntentDslParser.RBRACE, 0); }
		public List<TerminalNode> DIV() { return getTokens(NCIntentDslParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(NCIntentDslParser.DIV, i);
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
			setState(134);
			match(T__4);
			setState(136);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAR) {
				{
				setState(135);
				termId();
				}
			}

			setState(138);
			termEq();
			setState(147);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				{
				{
				setState(139);
				match(LBRACE);
				setState(140);
				expr(0);
				setState(141);
				match(RBRACE);
				}
				}
				break;
			case DIV:
				{
				{
				setState(143);
				match(DIV);
				setState(144);
				clsNer();
				setState(145);
				match(DIV);
				}
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(150);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				{
				setState(149);
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
			setState(153);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(152);
				javaFqn(0);
				}
			}

			setState(155);
			match(POUND);
			setState(156);
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
			setState(159);
			match(ID);
			}
			_ctx.stop = _input.LT(-1);
			setState(166);
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
					setState(161);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(162);
					match(DOT);
					setState(163);
					match(ID);
					}
					} 
				}
				setState(168);
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
		public TerminalNode LPAR() { return getToken(NCIntentDslParser.LPAR, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode RPAR() { return getToken(NCIntentDslParser.RPAR, 0); }
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
			setState(169);
			match(LPAR);
			setState(170);
			match(ID);
			setState(171);
			match(RPAR);
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
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ParExprContext extends ExprContext {
		public TerminalNode LPAR() { return getToken(NCIntentDslParser.LPAR, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIntentDslParser.RPAR, 0); }
		public ParExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterParExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitParExpr(this);
		}
	}
	public static class EqExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(NCIntentDslParser.NEQ, 0); }
		public EqExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterEqExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitEqExpr(this);
		}
	}
	public static class UnaryExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(NCIntentDslParser.NOT, 0); }
		public UnaryExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterUnaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitUnaryExpr(this);
		}
	}
	public static class CompExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode LTEQ() { return getToken(NCIntentDslParser.LTEQ, 0); }
		public TerminalNode GTEQ() { return getToken(NCIntentDslParser.GTEQ, 0); }
		public TerminalNode LT() { return getToken(NCIntentDslParser.LT, 0); }
		public TerminalNode GT() { return getToken(NCIntentDslParser.GT, 0); }
		public CompExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterCompExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitCompExpr(this);
		}
	}
	public static class AtomExprContext extends ExprContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AtomExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterAtomExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitAtomExpr(this);
		}
	}
	public static class CallExprContext extends ExprContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LPAR() { return getToken(NCIntentDslParser.LPAR, 0); }
		public TerminalNode RPAR() { return getToken(NCIntentDslParser.RPAR, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public CallExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterCallExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitCallExpr(this);
		}
	}
	public static class MultExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode MULT() { return getToken(NCIntentDslParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(NCIntentDslParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(NCIntentDslParser.MOD, 0); }
		public MultExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMultExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMultExpr(this);
		}
	}
	public static class ListExprContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public ListExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterListExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitListExpr(this);
		}
	}
	public static class PlusExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(NCIntentDslParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public PlusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterPlusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitPlusExpr(this);
		}
	}
	public static class LogExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode AND() { return getToken(NCIntentDslParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCIntentDslParser.OR, 0); }
		public LogExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLogExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLogExpr(this);
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
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case MINUS:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(174);
				((UnaryExprContext)_localctx).op = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==NOT || _la==MINUS) ) {
					((UnaryExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(175);
				expr(10);
				}
				break;
			case LPAR:
				{
				_localctx = new ParExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(176);
				match(LPAR);
				setState(177);
				expr(0);
				setState(178);
				match(RPAR);
				}
				break;
			case SQSTRING:
			case DQSTRING:
			case BOOL:
			case NULL:
			case INT:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(180);
				atom();
				}
				break;
			case ID:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(181);
				match(ID);
				setState(182);
				match(LPAR);
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << SQSTRING) | (1L << DQSTRING) | (1L << BOOL) | (1L << NULL) | (1L << NOT) | (1L << LPAR) | (1L << MINUS) | (1L << INT) | (1L << ID))) != 0)) {
					{
					setState(183);
					expr(0);
					}
				}

				setState(186);
				match(RPAR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(209);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(207);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
					case 1:
						{
						_localctx = new MultExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(189);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(190);
						((MultExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MULT) | (1L << DIV) | (1L << MOD))) != 0)) ) {
							((MultExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(191);
						expr(9);
						}
						break;
					case 2:
						{
						_localctx = new PlusExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(192);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(193);
						((PlusExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==MINUS || _la==PLUS) ) {
							((PlusExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(194);
						expr(8);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(195);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(196);
						((CompExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << GTEQ) | (1L << LTEQ) | (1L << GT) | (1L << LT))) != 0)) ) {
							((CompExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(197);
						expr(7);
						}
						break;
					case 4:
						{
						_localctx = new EqExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(198);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(199);
						((EqExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EQ || _la==NEQ) ) {
							((EqExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(200);
						expr(6);
						}
						break;
					case 5:
						{
						_localctx = new LogExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(201);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(202);
						((LogExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==OR) ) {
							((LogExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(203);
						expr(5);
						}
						break;
					case 6:
						{
						_localctx = new ListExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(204);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(205);
						match(COMMA);
						setState(206);
						expr(2);
						}
						break;
					}
					} 
				}
				setState(211);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
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

	public static class AtomContext extends ParserRuleContext {
		public TerminalNode NULL() { return getToken(NCIntentDslParser.NULL, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode REAL() { return getToken(NCIntentDslParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIntentDslParser.EXP, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitAtom(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_atom);
		try {
			setState(222);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(212);
				match(NULL);
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(213);
				match(INT);
				setState(215);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
				case 1:
					{
					setState(214);
					match(REAL);
					}
					break;
				}
				setState(218);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
				case 1:
					{
					setState(217);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(220);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(221);
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
		enterRule(_localctx, 34, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(224);
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
		enterRule(_localctx, 36, RULE_minMax);
		try {
			setState(228);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case MULT:
				enterOuterAlt(_localctx, 1);
				{
				setState(226);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(227);
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
		public TerminalNode MULT() { return getToken(NCIntentDslParser.MULT, 0); }
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
		enterRule(_localctx, 38, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << PLUS) | (1L << QUESTION) | (1L << MULT))) != 0)) ) {
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
		enterRule(_localctx, 40, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(232);
			match(LBR);
			setState(233);
			match(INT);
			setState(234);
			match(COMMA);
			setState(235);
			match(INT);
			setState(236);
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
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 8);
		case 3:
			return precpred(_ctx, 7);
		case 4:
			return precpred(_ctx, 6);
		case 5:
			return precpred(_ctx, 5);
		case 6:
			return precpred(_ctx, 4);
		case 7:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\61\u00f1\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\5\2/\n\2\3\2\5\2\62\n"+
		"\2\3\2\5\2\65\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5"+
		"\3\5\3\5\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\7\7N\n\7\f\7\16\7Q\13\7\3\7\3"+
		"\7\3\7\3\7\5\7W\n\7\3\b\3\b\3\b\3\b\3\t\3\t\5\t_\n\t\3\t\3\t\5\tc\n\t"+
		"\3\t\5\tf\n\t\3\t\3\t\3\t\3\t\5\tl\n\t\3\n\3\n\3\n\3\n\7\nr\n\n\f\n\16"+
		"\nu\13\n\3\n\3\n\3\n\3\n\5\n{\n\n\3\13\3\13\3\13\3\13\3\13\7\13\u0082"+
		"\n\13\f\13\16\13\u0085\13\13\3\f\3\f\3\r\3\r\5\r\u008b\n\r\3\r\3\r\3\r"+
		"\3\r\3\r\3\r\3\r\3\r\3\r\5\r\u0096\n\r\3\r\5\r\u0099\n\r\3\16\5\16\u009c"+
		"\n\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\7\17\u00a7\n\17\f\17"+
		"\16\17\u00aa\13\17\3\20\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\21\3"+
		"\21\3\21\3\21\3\21\3\21\5\21\u00bb\n\21\3\21\5\21\u00be\n\21\3\21\3\21"+
		"\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21\3\21"+
		"\3\21\3\21\7\21\u00d2\n\21\f\21\16\21\u00d5\13\21\3\22\3\22\3\22\5\22"+
		"\u00da\n\22\3\22\5\22\u00dd\n\22\3\22\3\22\5\22\u00e1\n\22\3\23\3\23\3"+
		"\24\3\24\5\24\u00e7\n\24\3\25\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26"+
		"\2\5\24\34 \27\2\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*\2\13\4\2"+
		"\34\34%%\4\2\25\25\"\"\3\2(*\4\2\"\"&&\3\2\16\21\3\2\f\r\3\2\22\23\3\2"+
		"\b\t\3\2&(\2\u0100\2,\3\2\2\2\49\3\2\2\2\6=\3\2\2\2\bA\3\2\2\2\nE\3\2"+
		"\2\2\fV\3\2\2\2\16X\3\2\2\2\20k\3\2\2\2\22z\3\2\2\2\24|\3\2\2\2\26\u0086"+
		"\3\2\2\2\30\u0088\3\2\2\2\32\u009b\3\2\2\2\34\u00a0\3\2\2\2\36\u00ab\3"+
		"\2\2\2 \u00bd\3\2\2\2\"\u00e0\3\2\2\2$\u00e2\3\2\2\2&\u00e6\3\2\2\2(\u00e8"+
		"\3\2\2\2*\u00ea\3\2\2\2,.\5\4\3\2-/\5\6\4\2.-\3\2\2\2./\3\2\2\2/\61\3"+
		"\2\2\2\60\62\5\b\5\2\61\60\3\2\2\2\61\62\3\2\2\2\62\64\3\2\2\2\63\65\5"+
		"\n\6\2\64\63\3\2\2\2\64\65\3\2\2\2\65\66\3\2\2\2\66\67\5\24\13\2\678\7"+
		"\2\2\38\3\3\2\2\29:\7\3\2\2:;\7%\2\2;<\7/\2\2<\5\3\2\2\2=>\7\4\2\2>?\7"+
		"%\2\2?@\7\n\2\2@\7\3\2\2\2AB\7\5\2\2BC\7%\2\2CD\5$\23\2D\t\3\2\2\2EF\7"+
		"\6\2\2FG\7%\2\2GH\5\f\7\2H\13\3\2\2\2IJ\7\30\2\2JO\5\16\b\2KL\7 \2\2L"+
		"N\5\16\b\2MK\3\2\2\2NQ\3\2\2\2OM\3\2\2\2OP\3\2\2\2PR\3\2\2\2QO\3\2\2\2"+
		"RS\7\31\2\2SW\3\2\2\2TU\7\30\2\2UW\7\31\2\2VI\3\2\2\2VT\3\2\2\2W\r\3\2"+
		"\2\2XY\5$\23\2YZ\7!\2\2Z[\5\20\t\2[\17\3\2\2\2\\l\5$\23\2]_\7\"\2\2^]"+
		"\3\2\2\2^_\3\2\2\2_`\3\2\2\2`b\7,\2\2ac\7-\2\2ba\3\2\2\2bc\3\2\2\2ce\3"+
		"\2\2\2df\7.\2\2ed\3\2\2\2ef\3\2\2\2fl\3\2\2\2gl\5\f\7\2hl\5\22\n\2il\7"+
		"\n\2\2jl\7\13\2\2k\\\3\2\2\2k^\3\2\2\2kg\3\2\2\2kh\3\2\2\2ki\3\2\2\2k"+
		"j\3\2\2\2l\21\3\2\2\2mn\7\35\2\2ns\5\20\t\2op\7 \2\2pr\5\20\t\2qo\3\2"+
		"\2\2ru\3\2\2\2sq\3\2\2\2st\3\2\2\2tv\3\2\2\2us\3\2\2\2vw\7\36\2\2w{\3"+
		"\2\2\2xy\7\35\2\2y{\7\36\2\2zm\3\2\2\2zx\3\2\2\2{\23\3\2\2\2|}\b\13\1"+
		"\2}~\5\30\r\2~\u0083\3\2\2\2\177\u0080\f\3\2\2\u0080\u0082\5\30\r\2\u0081"+
		"\177\3\2\2\2\u0082\u0085\3\2\2\2\u0083\u0081\3\2\2\2\u0083\u0084\3\2\2"+
		"\2\u0084\25\3\2\2\2\u0085\u0083\3\2\2\2\u0086\u0087\t\2\2\2\u0087\27\3"+
		"\2\2\2\u0088\u008a\7\7\2\2\u0089\u008b\5\36\20\2\u008a\u0089\3\2\2\2\u008a"+
		"\u008b\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u0095\5\26\f\2\u008d\u008e\7"+
		"\30\2\2\u008e\u008f\5 \21\2\u008f\u0090\7\31\2\2\u0090\u0096\3\2\2\2\u0091"+
		"\u0092\7)\2\2\u0092\u0093\5\32\16\2\u0093\u0094\7)\2\2\u0094\u0096\3\2"+
		"\2\2\u0095\u008d\3\2\2\2\u0095\u0091\3\2\2\2\u0096\u0098\3\2\2\2\u0097"+
		"\u0099\5&\24\2\u0098\u0097\3\2\2\2\u0098\u0099\3\2\2\2\u0099\31\3\2\2"+
		"\2\u009a\u009c\5\34\17\2\u009b\u009a\3\2\2\2\u009b\u009c\3\2\2\2\u009c"+
		"\u009d\3\2\2\2\u009d\u009e\7\37\2\2\u009e\u009f\7/\2\2\u009f\33\3\2\2"+
		"\2\u00a0\u00a1\b\17\1\2\u00a1\u00a2\7/\2\2\u00a2\u00a8\3\2\2\2\u00a3\u00a4"+
		"\f\3\2\2\u00a4\u00a5\7#\2\2\u00a5\u00a7\7/\2\2\u00a6\u00a3\3\2\2\2\u00a7"+
		"\u00aa\3\2\2\2\u00a8\u00a6\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\35\3\2\2"+
		"\2\u00aa\u00a8\3\2\2\2\u00ab\u00ac\7\26\2\2\u00ac\u00ad\7/\2\2\u00ad\u00ae"+
		"\7\27\2\2\u00ae\37\3\2\2\2\u00af\u00b0\b\21\1\2\u00b0\u00b1\t\3\2\2\u00b1"+
		"\u00be\5 \21\f\u00b2\u00b3\7\26\2\2\u00b3\u00b4\5 \21\2\u00b4\u00b5\7"+
		"\27\2\2\u00b5\u00be\3\2\2\2\u00b6\u00be\5\"\22\2\u00b7\u00b8\7/\2\2\u00b8"+
		"\u00ba\7\26\2\2\u00b9\u00bb\5 \21\2\u00ba\u00b9\3\2\2\2\u00ba\u00bb\3"+
		"\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00be\7\27\2\2\u00bd\u00af\3\2\2\2\u00bd"+
		"\u00b2\3\2\2\2\u00bd\u00b6\3\2\2\2\u00bd\u00b7\3\2\2\2\u00be\u00d3\3\2"+
		"\2\2\u00bf\u00c0\f\n\2\2\u00c0\u00c1\t\4\2\2\u00c1\u00d2\5 \21\13\u00c2"+
		"\u00c3\f\t\2\2\u00c3\u00c4\t\5\2\2\u00c4\u00d2\5 \21\n\u00c5\u00c6\f\b"+
		"\2\2\u00c6\u00c7\t\6\2\2\u00c7\u00d2\5 \21\t\u00c8\u00c9\f\7\2\2\u00c9"+
		"\u00ca\t\7\2\2\u00ca\u00d2\5 \21\b\u00cb\u00cc\f\6\2\2\u00cc\u00cd\t\b"+
		"\2\2\u00cd\u00d2\5 \21\7\u00ce\u00cf\f\3\2\2\u00cf\u00d0\7 \2\2\u00d0"+
		"\u00d2\5 \21\4\u00d1\u00bf\3\2\2\2\u00d1\u00c2\3\2\2\2\u00d1\u00c5\3\2"+
		"\2\2\u00d1\u00c8\3\2\2\2\u00d1\u00cb\3\2\2\2\u00d1\u00ce\3\2\2\2\u00d2"+
		"\u00d5\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d4!\3\2\2\2"+
		"\u00d5\u00d3\3\2\2\2\u00d6\u00e1\7\13\2\2\u00d7\u00d9\7,\2\2\u00d8\u00da"+
		"\7-\2\2\u00d9\u00d8\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00dc\3\2\2\2\u00db"+
		"\u00dd\7.\2\2\u00dc\u00db\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd\u00e1\3\2"+
		"\2\2\u00de\u00e1\7\n\2\2\u00df\u00e1\5$\23\2\u00e0\u00d6\3\2\2\2\u00e0"+
		"\u00d7\3\2\2\2\u00e0\u00de\3\2\2\2\u00e0\u00df\3\2\2\2\u00e1#\3\2\2\2"+
		"\u00e2\u00e3\t\t\2\2\u00e3%\3\2\2\2\u00e4\u00e7\5(\25\2\u00e5\u00e7\5"+
		"*\26\2\u00e6\u00e4\3\2\2\2\u00e6\u00e5\3\2\2\2\u00e7\'\3\2\2\2\u00e8\u00e9"+
		"\t\n\2\2\u00e9)\3\2\2\2\u00ea\u00eb\7\35\2\2\u00eb\u00ec\7,\2\2\u00ec"+
		"\u00ed\7 \2\2\u00ed\u00ee\7,\2\2\u00ee\u00ef\7\36\2\2\u00ef+\3\2\2\2\33"+
		".\61\64OV^beksz\u0083\u008a\u0095\u0098\u009b\u00a8\u00ba\u00bd\u00d1"+
		"\u00d3\u00d9\u00dc\u00e0\u00e6";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}