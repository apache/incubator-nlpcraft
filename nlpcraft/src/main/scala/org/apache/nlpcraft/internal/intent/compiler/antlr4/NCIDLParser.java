// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/internal/intent/compiler/antlr4\NCIDL.g4 by ANTLR 4.9.2
package org.apache.nlpcraft.internal.intent.compiler.antlr4;

import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.RuntimeMetaData;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.VocabularyImpl;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIDLParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FUN_NAME=1, IMPORT=2, INTENT=3, OPTIONS=4, FLOW=5, META=6, TERM=7, FRAG=8, 
		SQSTRING=9, DQSTRING=10, BOOL=11, NULL=12, EQ=13, NEQ=14, GTEQ=15, LTEQ=16, 
		GT=17, LT=18, AND=19, OR=20, VERT=21, NOT=22, LPAR=23, RPAR=24, LBRACE=25, 
		RBRACE=26, SQUOTE=27, DQUOTE=28, TILDA=29, LBR=30, RBR=31, POUND=32, COMMA=33, 
		COLON=34, MINUS=35, DOT=36, UNDERSCORE=37, ASSIGN=38, PLUS=39, QUESTION=40, 
		MULT=41, DIV=42, MOD=43, AT=44, DOLLAR=45, INT=46, REAL=47, EXP=48, ID=49, 
		COMMENT=50, WS=51, ErrorChar=52;
	public static final int
		RULE_idl = 0, RULE_idlDecls = 1, RULE_idlDecl = 2, RULE_imprt = 3, RULE_frag = 4, 
		RULE_fragId = 5, RULE_fragRef = 6, RULE_fragMeta = 7, RULE_intent = 8, 
		RULE_intentId = 9, RULE_flowDecl = 10, RULE_metaDecl = 11, RULE_optDecl = 12, 
		RULE_jsonObj = 13, RULE_jsonPair = 14, RULE_jsonVal = 15, RULE_jsonArr = 16, 
		RULE_termDecls = 17, RULE_termDecl = 18, RULE_termEq = 19, RULE_term = 20, 
		RULE_mtdRef = 21, RULE_javaFqn = 22, RULE_javaClass = 23, RULE_termId = 24, 
		RULE_expr = 25, RULE_vars = 26, RULE_varDecl = 27, RULE_paramList = 28, 
		RULE_atom = 29, RULE_qstring = 30, RULE_minMax = 31, RULE_minMaxShortcut = 32, 
		RULE_minMaxRange = 33, RULE_id = 34;
	private static String[] makeRuleNames() {
		return new String[] {
			"idl", "idlDecls", "idlDecl", "imprt", "frag", "fragId", "fragRef", "fragMeta", 
			"intent", "intentId", "flowDecl", "metaDecl", "optDecl", "jsonObj", "jsonPair", 
			"jsonVal", "jsonArr", "termDecls", "termDecl", "termEq", "term", "mtdRef", 
			"javaFqn", "javaClass", "termId", "expr", "vars", "varDecl", "paramList", 
			"atom", "qstring", "minMax", "minMaxShortcut", "minMaxRange", "id"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'import'", "'intent'", "'options'", "'flow'", "'meta'", 
			"'term'", "'fragment'", null, null, null, "'null'", "'=='", "'!='", "'>='", 
			"'<='", "'>'", "'<'", "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", 
			"'}'", "'''", "'\"'", "'~'", "'['", "']'", "'#'", "','", "':'", "'-'", 
			"'.'", "'_'", "'='", "'+'", "'?'", "'*'", "'/'", "'%'", "'@'", "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "FUN_NAME", "IMPORT", "INTENT", "OPTIONS", "FLOW", "META", "TERM", 
			"FRAG", "SQSTRING", "DQSTRING", "BOOL", "NULL", "EQ", "NEQ", "GTEQ", 
			"LTEQ", "GT", "LT", "AND", "OR", "VERT", "NOT", "LPAR", "RPAR", "LBRACE", 
			"RBRACE", "SQUOTE", "DQUOTE", "TILDA", "LBR", "RBR", "POUND", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "ASSIGN", "PLUS", "QUESTION", 
			"MULT", "DIV", "MOD", "AT", "DOLLAR", "INT", "REAL", "EXP", "ID", "COMMENT", 
			"WS", "ErrorChar"
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
	public String getGrammarFileName() { return "NCIDL.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCIDLParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class IdlContext extends ParserRuleContext {
		public IdlDeclsContext idlDecls() {
			return getRuleContext(IdlDeclsContext.class,0);
		}
		public TerminalNode EOF() { return getToken(NCIDLParser.EOF, 0); }
		public IdlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterIdl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitIdl(this);
		}
	}

	public final IdlContext idl() throws RecognitionException {
		IdlContext _localctx = new IdlContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_idl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			idlDecls(0);
			setState(71);
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

	public static class IdlDeclsContext extends ParserRuleContext {
		public IdlDeclContext idlDecl() {
			return getRuleContext(IdlDeclContext.class,0);
		}
		public IdlDeclsContext idlDecls() {
			return getRuleContext(IdlDeclsContext.class,0);
		}
		public IdlDeclsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idlDecls; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterIdlDecls(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitIdlDecls(this);
		}
	}

	public final IdlDeclsContext idlDecls() throws RecognitionException {
		return idlDecls(0);
	}

	private IdlDeclsContext idlDecls(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		IdlDeclsContext _localctx = new IdlDeclsContext(_ctx, _parentState);
		IdlDeclsContext _prevctx = _localctx;
		int _startState = 2;
		enterRecursionRule(_localctx, 2, RULE_idlDecls, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(74);
			idlDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(80);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new IdlDeclsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_idlDecls);
					setState(76);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(77);
					idlDecl();
					}
					} 
				}
				setState(82);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
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

	public static class IdlDeclContext extends ParserRuleContext {
		public IntentContext intent() {
			return getRuleContext(IntentContext.class,0);
		}
		public FragContext frag() {
			return getRuleContext(FragContext.class,0);
		}
		public ImprtContext imprt() {
			return getRuleContext(ImprtContext.class,0);
		}
		public IdlDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idlDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterIdlDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitIdlDecl(this);
		}
	}

	public final IdlDeclContext idlDecl() throws RecognitionException {
		IdlDeclContext _localctx = new IdlDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_idlDecl);
		try {
			setState(86);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(83);
				intent();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(84);
				frag();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 3);
				{
				setState(85);
				imprt();
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

	public static class ImprtContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(NCIDLParser.IMPORT, 0); }
		public TerminalNode LPAR() { return getToken(NCIDLParser.LPAR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIDLParser.RPAR, 0); }
		public ImprtContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imprt; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterImprt(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitImprt(this);
		}
	}

	public final ImprtContext imprt() throws RecognitionException {
		ImprtContext _localctx = new ImprtContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_imprt);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(IMPORT);
			setState(89);
			match(LPAR);
			setState(90);
			qstring();
			setState(91);
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

	public static class FragContext extends ParserRuleContext {
		public FragIdContext fragId() {
			return getRuleContext(FragIdContext.class,0);
		}
		public TermDeclsContext termDecls() {
			return getRuleContext(TermDeclsContext.class,0);
		}
		public FragContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_frag; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterFrag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitFrag(this);
		}
	}

	public final FragContext frag() throws RecognitionException {
		FragContext _localctx = new FragContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_frag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			fragId();
			setState(94);
			termDecls(0);
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

	public static class FragIdContext extends ParserRuleContext {
		public TerminalNode FRAG() { return getToken(NCIDLParser.FRAG, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public FragIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterFragId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitFragId(this);
		}
	}

	public final FragIdContext fragId() throws RecognitionException {
		FragIdContext _localctx = new FragIdContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_fragId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(96);
			match(FRAG);
			setState(97);
			match(ASSIGN);
			setState(98);
			id();
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

	public static class FragRefContext extends ParserRuleContext {
		public TerminalNode FRAG() { return getToken(NCIDLParser.FRAG, 0); }
		public TerminalNode LPAR() { return getToken(NCIDLParser.LPAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIDLParser.RPAR, 0); }
		public FragMetaContext fragMeta() {
			return getRuleContext(FragMetaContext.class,0);
		}
		public FragRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterFragRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitFragRef(this);
		}
	}

	public final FragRefContext fragRef() throws RecognitionException {
		FragRefContext _localctx = new FragRefContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_fragRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100);
			match(FRAG);
			setState(101);
			match(LPAR);
			setState(102);
			id();
			setState(104);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(103);
				fragMeta();
				}
			}

			setState(106);
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

	public static class FragMetaContext extends ParserRuleContext {
		public TerminalNode COMMA() { return getToken(NCIDLParser.COMMA, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public FragMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterFragMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitFragMeta(this);
		}
	}

	public final FragMetaContext fragMeta() throws RecognitionException {
		FragMetaContext _localctx = new FragMetaContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fragMeta);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(108);
			match(COMMA);
			setState(109);
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

	public static class IntentContext extends ParserRuleContext {
		public IntentIdContext intentId() {
			return getRuleContext(IntentIdContext.class,0);
		}
		public TermDeclsContext termDecls() {
			return getRuleContext(TermDeclsContext.class,0);
		}
		public OptDeclContext optDecl() {
			return getRuleContext(OptDeclContext.class,0);
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
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterIntent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitIntent(this);
		}
	}

	public final IntentContext intent() throws RecognitionException {
		IntentContext _localctx = new IntentContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_intent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(111);
			intentId();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPTIONS) {
				{
				setState(112);
				optDecl();
				}
			}

			setState(116);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FLOW) {
				{
				setState(115);
				flowDecl();
				}
			}

			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==META) {
				{
				setState(118);
				metaDecl();
				}
			}

			setState(121);
			termDecls(0);
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
		public TerminalNode INTENT() { return getToken(NCIDLParser.INTENT, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public IntentIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intentId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterIntentId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitIntentId(this);
		}
	}

	public final IntentIdContext intentId() throws RecognitionException {
		IntentIdContext _localctx = new IntentIdContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_intentId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			match(INTENT);
			setState(124);
			match(ASSIGN);
			setState(125);
			id();
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
		public TerminalNode FLOW() { return getToken(NCIDLParser.FLOW, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public FlowDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterFlowDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitFlowDecl(this);
		}
	}

	public final FlowDeclContext flowDecl() throws RecognitionException {
		FlowDeclContext _localctx = new FlowDeclContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_flowDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(FLOW);
			setState(128);
			match(ASSIGN);
			setState(129);
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
		public TerminalNode META() { return getToken(NCIDLParser.META, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public MetaDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMetaDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMetaDecl(this);
		}
	}

	public final MetaDeclContext metaDecl() throws RecognitionException {
		MetaDeclContext _localctx = new MetaDeclContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_metaDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			match(META);
			setState(132);
			match(ASSIGN);
			setState(133);
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

	public static class OptDeclContext extends ParserRuleContext {
		public TerminalNode OPTIONS() { return getToken(NCIDLParser.OPTIONS, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public OptDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_optDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterOptDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitOptDecl(this);
		}
	}

	public final OptDeclContext optDecl() throws RecognitionException {
		OptDeclContext _localctx = new OptDeclContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_optDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(135);
			match(OPTIONS);
			setState(136);
			match(ASSIGN);
			setState(137);
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
		public TerminalNode LBRACE() { return getToken(NCIDLParser.LBRACE, 0); }
		public List<JsonPairContext> jsonPair() {
			return getRuleContexts(JsonPairContext.class);
		}
		public JsonPairContext jsonPair(int i) {
			return getRuleContext(JsonPairContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(NCIDLParser.RBRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NCIDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NCIDLParser.COMMA, i);
		}
		public JsonObjContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonObj; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterJsonObj(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitJsonObj(this);
		}
	}

	public final JsonObjContext jsonObj() throws RecognitionException {
		JsonObjContext _localctx = new JsonObjContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_jsonObj);
		int _la;
		try {
			setState(152);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(139);
				match(LBRACE);
				setState(140);
				jsonPair();
				setState(145);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(141);
					match(COMMA);
					setState(142);
					jsonPair();
					}
					}
					setState(147);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(148);
				match(RBRACE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(150);
				match(LBRACE);
				setState(151);
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
		public TerminalNode COLON() { return getToken(NCIDLParser.COLON, 0); }
		public JsonValContext jsonVal() {
			return getRuleContext(JsonValContext.class,0);
		}
		public JsonPairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonPair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterJsonPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitJsonPair(this);
		}
	}

	public final JsonPairContext jsonPair() throws RecognitionException {
		JsonPairContext _localctx = new JsonPairContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_jsonPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			qstring();
			setState(155);
			match(COLON);
			setState(156);
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
		public TerminalNode INT() { return getToken(NCIDLParser.INT, 0); }
		public TerminalNode MINUS() { return getToken(NCIDLParser.MINUS, 0); }
		public TerminalNode REAL() { return getToken(NCIDLParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIDLParser.EXP, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public JsonArrContext jsonArr() {
			return getRuleContext(JsonArrContext.class,0);
		}
		public TerminalNode BOOL() { return getToken(NCIDLParser.BOOL, 0); }
		public TerminalNode NULL() { return getToken(NCIDLParser.NULL, 0); }
		public JsonValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterJsonVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitJsonVal(this);
		}
	}

	public final JsonValContext jsonVal() throws RecognitionException {
		JsonValContext _localctx = new JsonValContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_jsonVal);
		int _la;
		try {
			setState(173);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				qstring();
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(160);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(159);
					match(MINUS);
					}
				}

				setState(162);
				match(INT);
				setState(164);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REAL) {
					{
					setState(163);
					match(REAL);
					}
				}

				setState(167);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXP) {
					{
					setState(166);
					match(EXP);
					}
				}

				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(169);
				jsonObj();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 4);
				{
				setState(170);
				jsonArr();
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 5);
				{
				setState(171);
				match(BOOL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(172);
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
		public TerminalNode LBR() { return getToken(NCIDLParser.LBR, 0); }
		public List<JsonValContext> jsonVal() {
			return getRuleContexts(JsonValContext.class);
		}
		public JsonValContext jsonVal(int i) {
			return getRuleContext(JsonValContext.class,i);
		}
		public TerminalNode RBR() { return getToken(NCIDLParser.RBR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NCIDLParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NCIDLParser.COMMA, i);
		}
		public JsonArrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonArr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterJsonArr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitJsonArr(this);
		}
	}

	public final JsonArrContext jsonArr() throws RecognitionException {
		JsonArrContext _localctx = new JsonArrContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_jsonArr);
		int _la;
		try {
			setState(188);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(175);
				match(LBR);
				setState(176);
				jsonVal();
				setState(181);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(177);
					match(COMMA);
					setState(178);
					jsonVal();
					}
					}
					setState(183);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(184);
				match(RBR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(186);
				match(LBR);
				setState(187);
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

	public static class TermDeclsContext extends ParserRuleContext {
		public TermDeclContext termDecl() {
			return getRuleContext(TermDeclContext.class,0);
		}
		public TermDeclsContext termDecls() {
			return getRuleContext(TermDeclsContext.class,0);
		}
		public TermDeclsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termDecls; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterTermDecls(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitTermDecls(this);
		}
	}

	public final TermDeclsContext termDecls() throws RecognitionException {
		return termDecls(0);
	}

	private TermDeclsContext termDecls(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		TermDeclsContext _localctx = new TermDeclsContext(_ctx, _parentState);
		TermDeclsContext _prevctx = _localctx;
		int _startState = 34;
		enterRecursionRule(_localctx, 34, RULE_termDecls, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(191);
			termDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(197);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermDeclsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_termDecls);
					setState(193);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(194);
					termDecl();
					}
					} 
				}
				setState(199);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
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

	public static class TermDeclContext extends ParserRuleContext {
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public FragRefContext fragRef() {
			return getRuleContext(FragRefContext.class,0);
		}
		public TermDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterTermDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitTermDecl(this);
		}
	}

	public final TermDeclContext termDecl() throws RecognitionException {
		TermDeclContext _localctx = new TermDeclContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_termDecl);
		try {
			setState(202);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(200);
				term();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(201);
				fragRef();
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

	public static class TermEqContext extends ParserRuleContext {
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public TerminalNode TILDA() { return getToken(NCIDLParser.TILDA, 0); }
		public TermEqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termEq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterTermEq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitTermEq(this);
		}
	}

	public final TermEqContext termEq() throws RecognitionException {
		TermEqContext _localctx = new TermEqContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(204);
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
		public TerminalNode TERM() { return getToken(NCIDLParser.TERM, 0); }
		public TermEqContext termEq() {
			return getRuleContext(TermEqContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(NCIDLParser.LBRACE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(NCIDLParser.RBRACE, 0); }
		public TermIdContext termId() {
			return getRuleContext(TermIdContext.class,0);
		}
		public VarsContext vars() {
			return getRuleContext(VarsContext.class,0);
		}
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(206);
			match(TERM);
			setState(208);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAR) {
				{
				setState(207);
				termId();
				}
			}

			setState(210);
			termEq();
			setState(211);
			match(LBRACE);
			setState(213);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(212);
				vars(0);
				}
				break;
			}
			setState(215);
			expr(0);
			setState(216);
			match(RBRACE);
			setState(218);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
			case 1:
				{
				setState(217);
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

	public static class MtdRefContext extends ParserRuleContext {
		public TerminalNode POUND() { return getToken(NCIDLParser.POUND, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public JavaFqnContext javaFqn() {
			return getRuleContext(JavaFqnContext.class,0);
		}
		public MtdRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mtdRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMtdRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMtdRef(this);
		}
	}

	public final MtdRefContext mtdRef() throws RecognitionException {
		MtdRefContext _localctx = new MtdRefContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_mtdRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(221);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << IMPORT) | (1L << INTENT) | (1L << OPTIONS) | (1L << FLOW) | (1L << META) | (1L << TERM) | (1L << FRAG) | (1L << ID))) != 0)) {
				{
				setState(220);
				javaFqn(0);
				}
			}

			setState(223);
			match(POUND);
			setState(224);
			id();
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
		public JavaClassContext javaClass() {
			return getRuleContext(JavaClassContext.class,0);
		}
		public JavaFqnContext javaFqn() {
			return getRuleContext(JavaFqnContext.class,0);
		}
		public TerminalNode DOT() { return getToken(NCIDLParser.DOT, 0); }
		public JavaFqnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_javaFqn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterJavaFqn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitJavaFqn(this);
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
		int _startState = 44;
		enterRecursionRule(_localctx, 44, RULE_javaFqn, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(227);
			javaClass();
			}
			_ctx.stop = _input.LT(-1);
			setState(234);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new JavaFqnContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_javaFqn);
					setState(229);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(230);
					match(DOT);
					setState(231);
					javaClass();
					}
					} 
				}
				setState(236);
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

	public static class JavaClassContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode IMPORT() { return getToken(NCIDLParser.IMPORT, 0); }
		public TerminalNode INTENT() { return getToken(NCIDLParser.INTENT, 0); }
		public TerminalNode OPTIONS() { return getToken(NCIDLParser.OPTIONS, 0); }
		public TerminalNode FLOW() { return getToken(NCIDLParser.FLOW, 0); }
		public TerminalNode META() { return getToken(NCIDLParser.META, 0); }
		public TerminalNode TERM() { return getToken(NCIDLParser.TERM, 0); }
		public TerminalNode FRAG() { return getToken(NCIDLParser.FRAG, 0); }
		public JavaClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_javaClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterJavaClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitJavaClass(this);
		}
	}

	public final JavaClassContext javaClass() throws RecognitionException {
		JavaClassContext _localctx = new JavaClassContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_javaClass);
		try {
			setState(245);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FUN_NAME:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(237);
				id();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 2);
				{
				setState(238);
				match(IMPORT);
				}
				break;
			case INTENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(239);
				match(INTENT);
				}
				break;
			case OPTIONS:
				enterOuterAlt(_localctx, 4);
				{
				setState(240);
				match(OPTIONS);
				}
				break;
			case FLOW:
				enterOuterAlt(_localctx, 5);
				{
				setState(241);
				match(FLOW);
				}
				break;
			case META:
				enterOuterAlt(_localctx, 6);
				{
				setState(242);
				match(META);
				}
				break;
			case TERM:
				enterOuterAlt(_localctx, 7);
				{
				setState(243);
				match(TERM);
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 8);
				{
				setState(244);
				match(FRAG);
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

	public static class TermIdContext extends ParserRuleContext {
		public TerminalNode LPAR() { return getToken(NCIDLParser.LPAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIDLParser.RPAR, 0); }
		public TermIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterTermId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitTermId(this);
		}
	}

	public final TermIdContext termId() throws RecognitionException {
		TermIdContext _localctx = new TermIdContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(247);
			match(LPAR);
			setState(248);
			id();
			setState(249);
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
		public TerminalNode LPAR() { return getToken(NCIDLParser.LPAR, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIDLParser.RPAR, 0); }
		public ParExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterParExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitParExpr(this);
		}
	}
	public static class UnaryExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(NCIDLParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(NCIDLParser.NOT, 0); }
		public UnaryExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterUnaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitUnaryExpr(this);
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
		public TerminalNode LTEQ() { return getToken(NCIDLParser.LTEQ, 0); }
		public TerminalNode GTEQ() { return getToken(NCIDLParser.GTEQ, 0); }
		public TerminalNode LT() { return getToken(NCIDLParser.LT, 0); }
		public TerminalNode GT() { return getToken(NCIDLParser.GT, 0); }
		public CompExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterCompExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitCompExpr(this);
		}
	}
	public static class PlusMinusExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode PLUS() { return getToken(NCIDLParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(NCIDLParser.MINUS, 0); }
		public PlusMinusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterPlusMinusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitPlusMinusExpr(this);
		}
	}
	public static class AtomExprContext extends ExprContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AtomExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterAtomExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitAtomExpr(this);
		}
	}
	public static class VarRefContext extends ExprContext {
		public TerminalNode AT() { return getToken(NCIDLParser.AT, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public VarRefContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterVarRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitVarRef(this);
		}
	}
	public static class MultDivModExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode MULT() { return getToken(NCIDLParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(NCIDLParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(NCIDLParser.MOD, 0); }
		public MultDivModExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMultDivModExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMultDivModExpr(this);
		}
	}
	public static class AndOrExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode AND() { return getToken(NCIDLParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCIDLParser.OR, 0); }
		public AndOrExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterAndOrExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitAndOrExpr(this);
		}
	}
	public static class CallExprContext extends ExprContext {
		public TerminalNode LPAR() { return getToken(NCIDLParser.LPAR, 0); }
		public TerminalNode RPAR() { return getToken(NCIDLParser.RPAR, 0); }
		public TerminalNode FUN_NAME() { return getToken(NCIDLParser.FUN_NAME, 0); }
		public TerminalNode POUND() { return getToken(NCIDLParser.POUND, 0); }
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public CallExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterCallExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitCallExpr(this);
		}
	}
	public static class EqNeqExprContext extends ExprContext {
		public Token op;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public TerminalNode EQ() { return getToken(NCIDLParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(NCIDLParser.NEQ, 0); }
		public EqNeqExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterEqNeqExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitEqNeqExpr(this);
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
		int _startState = 50;
		enterRecursionRule(_localctx, 50, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(268);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(252);
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
				setState(253);
				expr(11);
				}
				break;
			case 2:
				{
				_localctx = new ParExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(254);
				match(LPAR);
				setState(255);
				expr(0);
				setState(256);
				match(RPAR);
				}
				break;
			case 3:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(258);
				atom();
				}
				break;
			case 4:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(259);
				_la = _input.LA(1);
				if ( !(_la==FUN_NAME || _la==POUND) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(260);
				match(LPAR);
				setState(262);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << SQSTRING) | (1L << DQSTRING) | (1L << BOOL) | (1L << NULL) | (1L << NOT) | (1L << LPAR) | (1L << POUND) | (1L << MINUS) | (1L << AT) | (1L << INT))) != 0)) {
					{
					setState(261);
					paramList(0);
					}
				}

				setState(264);
				match(RPAR);
				}
				break;
			case 5:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(265);
				_la = _input.LA(1);
				if ( !(_la==FUN_NAME || _la==POUND) ) {
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
				{
				_localctx = new VarRefContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(266);
				match(AT);
				setState(267);
				id();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(287);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(285);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
					case 1:
						{
						_localctx = new MultDivModExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(270);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(271);
						((MultDivModExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MULT) | (1L << DIV) | (1L << MOD))) != 0)) ) {
							((MultDivModExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(272);
						expr(10);
						}
						break;
					case 2:
						{
						_localctx = new PlusMinusExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(273);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(274);
						((PlusMinusExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==MINUS || _la==PLUS) ) {
							((PlusMinusExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(275);
						expr(9);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(276);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(277);
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
						setState(278);
						expr(8);
						}
						break;
					case 4:
						{
						_localctx = new EqNeqExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(279);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(280);
						((EqNeqExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==EQ || _la==NEQ) ) {
							((EqNeqExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(281);
						expr(7);
						}
						break;
					case 5:
						{
						_localctx = new AndOrExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(282);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(283);
						((AndOrExprContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==OR) ) {
							((AndOrExprContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(284);
						expr(6);
						}
						break;
					}
					} 
				}
				setState(289);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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

	public static class VarsContext extends ParserRuleContext {
		public VarDeclContext varDecl() {
			return getRuleContext(VarDeclContext.class,0);
		}
		public VarsContext vars() {
			return getRuleContext(VarsContext.class,0);
		}
		public VarsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_vars; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterVars(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitVars(this);
		}
	}

	public final VarsContext vars() throws RecognitionException {
		return vars(0);
	}

	private VarsContext vars(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		VarsContext _localctx = new VarsContext(_ctx, _parentState);
		VarsContext _prevctx = _localctx;
		int _startState = 52;
		enterRecursionRule(_localctx, 52, RULE_vars, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(291);
			varDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(297);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new VarsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_vars);
					setState(293);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(294);
					varDecl();
					}
					} 
				}
				setState(299);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,26,_ctx);
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

	public static class VarDeclContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(NCIDLParser.AT, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(NCIDLParser.ASSIGN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterVarDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitVarDecl(this);
		}
	}

	public final VarDeclContext varDecl() throws RecognitionException {
		VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_varDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(300);
			match(AT);
			setState(301);
			id();
			setState(302);
			match(ASSIGN);
			setState(303);
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

	public static class ParamListContext extends ParserRuleContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(NCIDLParser.COMMA, 0); }
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterParamList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitParamList(this);
		}
	}

	public final ParamListContext paramList() throws RecognitionException {
		return paramList(0);
	}

	private ParamListContext paramList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ParamListContext _localctx = new ParamListContext(_ctx, _parentState);
		ParamListContext _prevctx = _localctx;
		int _startState = 56;
		enterRecursionRule(_localctx, 56, RULE_paramList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(306);
			expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(313);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ParamListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_paramList);
					setState(308);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(309);
					match(COMMA);
					setState(310);
					expr(0);
					}
					} 
				}
				setState(315);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
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
		public TerminalNode NULL() { return getToken(NCIDLParser.NULL, 0); }
		public TerminalNode INT() { return getToken(NCIDLParser.INT, 0); }
		public TerminalNode REAL() { return getToken(NCIDLParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIDLParser.EXP, 0); }
		public TerminalNode BOOL() { return getToken(NCIDLParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitAtom(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_atom);
		try {
			setState(326);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(316);
				match(NULL);
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(317);
				match(INT);
				setState(319);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
				case 1:
					{
					setState(318);
					match(REAL);
					}
					break;
				}
				setState(322);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,29,_ctx) ) {
				case 1:
					{
					setState(321);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(324);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(325);
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
		public TerminalNode SQSTRING() { return getToken(NCIDLParser.SQSTRING, 0); }
		public TerminalNode DQSTRING() { return getToken(NCIDLParser.DQSTRING, 0); }
		public QstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterQstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitQstring(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(328);
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
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMinMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMinMax(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_minMax);
		try {
			setState(332);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case MULT:
				enterOuterAlt(_localctx, 1);
				{
				setState(330);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(331);
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
		public TerminalNode PLUS() { return getToken(NCIDLParser.PLUS, 0); }
		public TerminalNode QUESTION() { return getToken(NCIDLParser.QUESTION, 0); }
		public TerminalNode MULT() { return getToken(NCIDLParser.MULT, 0); }
		public MinMaxShortcutContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxShortcut; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMinMaxShortcut(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMinMaxShortcut(this);
		}
	}

	public final MinMaxShortcutContext minMaxShortcut() throws RecognitionException {
		MinMaxShortcutContext _localctx = new MinMaxShortcutContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(334);
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
		public TerminalNode LBR() { return getToken(NCIDLParser.LBR, 0); }
		public List<TerminalNode> INT() { return getTokens(NCIDLParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(NCIDLParser.INT, i);
		}
		public TerminalNode COMMA() { return getToken(NCIDLParser.COMMA, 0); }
		public TerminalNode RBR() { return getToken(NCIDLParser.RBR, 0); }
		public MinMaxRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMinMaxRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMinMaxRange(this);
		}
	}

	public final MinMaxRangeContext minMaxRange() throws RecognitionException {
		MinMaxRangeContext _localctx = new MinMaxRangeContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(336);
			match(LBR);
			setState(337);
			match(INT);
			setState(338);
			match(COMMA);
			setState(339);
			match(INT);
			setState(340);
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

	public static class IdContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIDLParser.ID, 0); }
		public TerminalNode FUN_NAME() { return getToken(NCIDLParser.FUN_NAME, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitId(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_id);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(342);
			_la = _input.LA(1);
			if ( !(_la==FUN_NAME || _la==ID) ) {
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
		case 1:
			return idlDecls_sempred((IdlDeclsContext)_localctx, predIndex);
		case 17:
			return termDecls_sempred((TermDeclsContext)_localctx, predIndex);
		case 22:
			return javaFqn_sempred((JavaFqnContext)_localctx, predIndex);
		case 25:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 26:
			return vars_sempred((VarsContext)_localctx, predIndex);
		case 28:
			return paramList_sempred((ParamListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean idlDecls_sempred(IdlDeclsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean termDecls_sempred(TermDeclsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean javaFqn_sempred(JavaFqnContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 9);
		case 4:
			return precpred(_ctx, 8);
		case 5:
			return precpred(_ctx, 7);
		case 6:
			return precpred(_ctx, 6);
		case 7:
			return precpred(_ctx, 5);
		}
		return true;
	}
	private boolean vars_sempred(VarsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 8:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean paramList_sempred(ParamListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 9:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\66\u015b\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\7\3Q\n\3\f\3\16"+
		"\3T\13\3\3\4\3\4\3\4\5\4Y\n\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7"+
		"\3\7\3\7\3\b\3\b\3\b\3\b\5\bk\n\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\5\nt\n\n"+
		"\3\n\5\nw\n\n\3\n\5\nz\n\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f\3\f\3\f"+
		"\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\7\17\u0092\n"+
		"\17\f\17\16\17\u0095\13\17\3\17\3\17\3\17\3\17\5\17\u009b\n\17\3\20\3"+
		"\20\3\20\3\20\3\21\3\21\5\21\u00a3\n\21\3\21\3\21\5\21\u00a7\n\21\3\21"+
		"\5\21\u00aa\n\21\3\21\3\21\3\21\3\21\5\21\u00b0\n\21\3\22\3\22\3\22\3"+
		"\22\7\22\u00b6\n\22\f\22\16\22\u00b9\13\22\3\22\3\22\3\22\3\22\5\22\u00bf"+
		"\n\22\3\23\3\23\3\23\3\23\3\23\7\23\u00c6\n\23\f\23\16\23\u00c9\13\23"+
		"\3\24\3\24\5\24\u00cd\n\24\3\25\3\25\3\26\3\26\5\26\u00d3\n\26\3\26\3"+
		"\26\3\26\5\26\u00d8\n\26\3\26\3\26\3\26\5\26\u00dd\n\26\3\27\5\27\u00e0"+
		"\n\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\30\7\30\u00eb\n\30\f\30"+
		"\16\30\u00ee\13\30\3\31\3\31\3\31\3\31\3\31\3\31\3\31\3\31\5\31\u00f8"+
		"\n\31\3\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\5\33\u0109\n\33\3\33\3\33\3\33\3\33\5\33\u010f\n\33\3\33\3"+
		"\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\3\33\7"+
		"\33\u0120\n\33\f\33\16\33\u0123\13\33\3\34\3\34\3\34\3\34\3\34\7\34\u012a"+
		"\n\34\f\34\16\34\u012d\13\34\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3"+
		"\36\3\36\3\36\7\36\u013a\n\36\f\36\16\36\u013d\13\36\3\37\3\37\3\37\5"+
		"\37\u0142\n\37\3\37\5\37\u0145\n\37\3\37\3\37\5\37\u0149\n\37\3 \3 \3"+
		"!\3!\5!\u014f\n!\3\"\3\"\3#\3#\3#\3#\3#\3#\3$\3$\3$\2\b\4$.\64\66:%\2"+
		"\4\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDF\2\r"+
		"\4\2\37\37((\4\2\30\30%%\4\2\3\3\"\"\3\2+-\4\2%%))\3\2\21\24\3\2\17\20"+
		"\3\2\25\26\3\2\13\f\3\2)+\4\2\3\3\63\63\2\u016b\2H\3\2\2\2\4K\3\2\2\2"+
		"\6X\3\2\2\2\bZ\3\2\2\2\n_\3\2\2\2\fb\3\2\2\2\16f\3\2\2\2\20n\3\2\2\2\22"+
		"q\3\2\2\2\24}\3\2\2\2\26\u0081\3\2\2\2\30\u0085\3\2\2\2\32\u0089\3\2\2"+
		"\2\34\u009a\3\2\2\2\36\u009c\3\2\2\2 \u00af\3\2\2\2\"\u00be\3\2\2\2$\u00c0"+
		"\3\2\2\2&\u00cc\3\2\2\2(\u00ce\3\2\2\2*\u00d0\3\2\2\2,\u00df\3\2\2\2."+
		"\u00e4\3\2\2\2\60\u00f7\3\2\2\2\62\u00f9\3\2\2\2\64\u010e\3\2\2\2\66\u0124"+
		"\3\2\2\28\u012e\3\2\2\2:\u0133\3\2\2\2<\u0148\3\2\2\2>\u014a\3\2\2\2@"+
		"\u014e\3\2\2\2B\u0150\3\2\2\2D\u0152\3\2\2\2F\u0158\3\2\2\2HI\5\4\3\2"+
		"IJ\7\2\2\3J\3\3\2\2\2KL\b\3\1\2LM\5\6\4\2MR\3\2\2\2NO\f\3\2\2OQ\5\6\4"+
		"\2PN\3\2\2\2QT\3\2\2\2RP\3\2\2\2RS\3\2\2\2S\5\3\2\2\2TR\3\2\2\2UY\5\22"+
		"\n\2VY\5\n\6\2WY\5\b\5\2XU\3\2\2\2XV\3\2\2\2XW\3\2\2\2Y\7\3\2\2\2Z[\7"+
		"\4\2\2[\\\7\31\2\2\\]\5> \2]^\7\32\2\2^\t\3\2\2\2_`\5\f\7\2`a\5$\23\2"+
		"a\13\3\2\2\2bc\7\n\2\2cd\7(\2\2de\5F$\2e\r\3\2\2\2fg\7\n\2\2gh\7\31\2"+
		"\2hj\5F$\2ik\5\20\t\2ji\3\2\2\2jk\3\2\2\2kl\3\2\2\2lm\7\32\2\2m\17\3\2"+
		"\2\2no\7#\2\2op\5\34\17\2p\21\3\2\2\2qs\5\24\13\2rt\5\32\16\2sr\3\2\2"+
		"\2st\3\2\2\2tv\3\2\2\2uw\5\26\f\2vu\3\2\2\2vw\3\2\2\2wy\3\2\2\2xz\5\30"+
		"\r\2yx\3\2\2\2yz\3\2\2\2z{\3\2\2\2{|\5$\23\2|\23\3\2\2\2}~\7\5\2\2~\177"+
		"\7(\2\2\177\u0080\5F$\2\u0080\25\3\2\2\2\u0081\u0082\7\7\2\2\u0082\u0083"+
		"\7(\2\2\u0083\u0084\5> \2\u0084\27\3\2\2\2\u0085\u0086\7\b\2\2\u0086\u0087"+
		"\7(\2\2\u0087\u0088\5\34\17\2\u0088\31\3\2\2\2\u0089\u008a\7\6\2\2\u008a"+
		"\u008b\7(\2\2\u008b\u008c\5\34\17\2\u008c\33\3\2\2\2\u008d\u008e\7\33"+
		"\2\2\u008e\u0093\5\36\20\2\u008f\u0090\7#\2\2\u0090\u0092\5\36\20\2\u0091"+
		"\u008f\3\2\2\2\u0092\u0095\3\2\2\2\u0093\u0091\3\2\2\2\u0093\u0094\3\2"+
		"\2\2\u0094\u0096\3\2\2\2\u0095\u0093\3\2\2\2\u0096\u0097\7\34\2\2\u0097"+
		"\u009b\3\2\2\2\u0098\u0099\7\33\2\2\u0099\u009b\7\34\2\2\u009a\u008d\3"+
		"\2\2\2\u009a\u0098\3\2\2\2\u009b\35\3\2\2\2\u009c\u009d\5> \2\u009d\u009e"+
		"\7$\2\2\u009e\u009f\5 \21\2\u009f\37\3\2\2\2\u00a0\u00b0\5> \2\u00a1\u00a3"+
		"\7%\2\2\u00a2\u00a1\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a4\3\2\2\2\u00a4"+
		"\u00a6\7\60\2\2\u00a5\u00a7\7\61\2\2\u00a6\u00a5\3\2\2\2\u00a6\u00a7\3"+
		"\2\2\2\u00a7\u00a9\3\2\2\2\u00a8\u00aa\7\62\2\2\u00a9\u00a8\3\2\2\2\u00a9"+
		"\u00aa\3\2\2\2\u00aa\u00b0\3\2\2\2\u00ab\u00b0\5\34\17\2\u00ac\u00b0\5"+
		"\"\22\2\u00ad\u00b0\7\r\2\2\u00ae\u00b0\7\16\2\2\u00af\u00a0\3\2\2\2\u00af"+
		"\u00a2\3\2\2\2\u00af\u00ab\3\2\2\2\u00af\u00ac\3\2\2\2\u00af\u00ad\3\2"+
		"\2\2\u00af\u00ae\3\2\2\2\u00b0!\3\2\2\2\u00b1\u00b2\7 \2\2\u00b2\u00b7"+
		"\5 \21\2\u00b3\u00b4\7#\2\2\u00b4\u00b6\5 \21\2\u00b5\u00b3\3\2\2\2\u00b6"+
		"\u00b9\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b7\u00b8\3\2\2\2\u00b8\u00ba\3\2"+
		"\2\2\u00b9\u00b7\3\2\2\2\u00ba\u00bb\7!\2\2\u00bb\u00bf\3\2\2\2\u00bc"+
		"\u00bd\7 \2\2\u00bd\u00bf\7!\2\2\u00be\u00b1\3\2\2\2\u00be\u00bc\3\2\2"+
		"\2\u00bf#\3\2\2\2\u00c0\u00c1\b\23\1\2\u00c1\u00c2\5&\24\2\u00c2\u00c7"+
		"\3\2\2\2\u00c3\u00c4\f\3\2\2\u00c4\u00c6\5&\24\2\u00c5\u00c3\3\2\2\2\u00c6"+
		"\u00c9\3\2\2\2\u00c7\u00c5\3\2\2\2\u00c7\u00c8\3\2\2\2\u00c8%\3\2\2\2"+
		"\u00c9\u00c7\3\2\2\2\u00ca\u00cd\5*\26\2\u00cb\u00cd\5\16\b\2\u00cc\u00ca"+
		"\3\2\2\2\u00cc\u00cb\3\2\2\2\u00cd\'\3\2\2\2\u00ce\u00cf\t\2\2\2\u00cf"+
		")\3\2\2\2\u00d0\u00d2\7\t\2\2\u00d1\u00d3\5\62\32\2\u00d2\u00d1\3\2\2"+
		"\2\u00d2\u00d3\3\2\2\2\u00d3\u00d4\3\2\2\2\u00d4\u00d5\5(\25\2\u00d5\u00d7"+
		"\7\33\2\2\u00d6\u00d8\5\66\34\2\u00d7\u00d6\3\2\2\2\u00d7\u00d8\3\2\2"+
		"\2\u00d8\u00d9\3\2\2\2\u00d9\u00da\5\64\33\2\u00da\u00dc\7\34\2\2\u00db"+
		"\u00dd\5@!\2\u00dc\u00db\3\2\2\2\u00dc\u00dd\3\2\2\2\u00dd+\3\2\2\2\u00de"+
		"\u00e0\5.\30\2\u00df\u00de\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e1\3\2"+
		"\2\2\u00e1\u00e2\7\"\2\2\u00e2\u00e3\5F$\2\u00e3-\3\2\2\2\u00e4\u00e5"+
		"\b\30\1\2\u00e5\u00e6\5\60\31\2\u00e6\u00ec\3\2\2\2\u00e7\u00e8\f\3\2"+
		"\2\u00e8\u00e9\7&\2\2\u00e9\u00eb\5\60\31\2\u00ea\u00e7\3\2\2\2\u00eb"+
		"\u00ee\3\2\2\2\u00ec\u00ea\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed/\3\2\2\2"+
		"\u00ee\u00ec\3\2\2\2\u00ef\u00f8\5F$\2\u00f0\u00f8\7\4\2\2\u00f1\u00f8"+
		"\7\5\2\2\u00f2\u00f8\7\6\2\2\u00f3\u00f8\7\7\2\2\u00f4\u00f8\7\b\2\2\u00f5"+
		"\u00f8\7\t\2\2\u00f6\u00f8\7\n\2\2\u00f7\u00ef\3\2\2\2\u00f7\u00f0\3\2"+
		"\2\2\u00f7\u00f1\3\2\2\2\u00f7\u00f2\3\2\2\2\u00f7\u00f3\3\2\2\2\u00f7"+
		"\u00f4\3\2\2\2\u00f7\u00f5\3\2\2\2\u00f7\u00f6\3\2\2\2\u00f8\61\3\2\2"+
		"\2\u00f9\u00fa\7\31\2\2\u00fa\u00fb\5F$\2\u00fb\u00fc\7\32\2\2\u00fc\63"+
		"\3\2\2\2\u00fd\u00fe\b\33\1\2\u00fe\u00ff\t\3\2\2\u00ff\u010f\5\64\33"+
		"\r\u0100\u0101\7\31\2\2\u0101\u0102\5\64\33\2\u0102\u0103\7\32\2\2\u0103"+
		"\u010f\3\2\2\2\u0104\u010f\5<\37\2\u0105\u0106\t\4\2\2\u0106\u0108\7\31"+
		"\2\2\u0107\u0109\5:\36\2\u0108\u0107\3\2\2\2\u0108\u0109\3\2\2\2\u0109"+
		"\u010a\3\2\2\2\u010a\u010f\7\32\2\2\u010b\u010f\t\4\2\2\u010c\u010d\7"+
		".\2\2\u010d\u010f\5F$\2\u010e\u00fd\3\2\2\2\u010e\u0100\3\2\2\2\u010e"+
		"\u0104\3\2\2\2\u010e\u0105\3\2\2\2\u010e\u010b\3\2\2\2\u010e\u010c\3\2"+
		"\2\2\u010f\u0121\3\2\2\2\u0110\u0111\f\13\2\2\u0111\u0112\t\5\2\2\u0112"+
		"\u0120\5\64\33\f\u0113\u0114\f\n\2\2\u0114\u0115\t\6\2\2\u0115\u0120\5"+
		"\64\33\13\u0116\u0117\f\t\2\2\u0117\u0118\t\7\2\2\u0118\u0120\5\64\33"+
		"\n\u0119\u011a\f\b\2\2\u011a\u011b\t\b\2\2\u011b\u0120\5\64\33\t\u011c"+
		"\u011d\f\7\2\2\u011d\u011e\t\t\2\2\u011e\u0120\5\64\33\b\u011f\u0110\3"+
		"\2\2\2\u011f\u0113\3\2\2\2\u011f\u0116\3\2\2\2\u011f\u0119\3\2\2\2\u011f"+
		"\u011c\3\2\2\2\u0120\u0123\3\2\2\2\u0121\u011f\3\2\2\2\u0121\u0122\3\2"+
		"\2\2\u0122\65\3\2\2\2\u0123\u0121\3\2\2\2\u0124\u0125\b\34\1\2\u0125\u0126"+
		"\58\35\2\u0126\u012b\3\2\2\2\u0127\u0128\f\3\2\2\u0128\u012a\58\35\2\u0129"+
		"\u0127\3\2\2\2\u012a\u012d\3\2\2\2\u012b\u0129\3\2\2\2\u012b\u012c\3\2"+
		"\2\2\u012c\67\3\2\2\2\u012d\u012b\3\2\2\2\u012e\u012f\7.\2\2\u012f\u0130"+
		"\5F$\2\u0130\u0131\7(\2\2\u0131\u0132\5\64\33\2\u01329\3\2\2\2\u0133\u0134"+
		"\b\36\1\2\u0134\u0135\5\64\33\2\u0135\u013b\3\2\2\2\u0136\u0137\f\3\2"+
		"\2\u0137\u0138\7#\2\2\u0138\u013a\5\64\33\2\u0139\u0136\3\2\2\2\u013a"+
		"\u013d\3\2\2\2\u013b\u0139\3\2\2\2\u013b\u013c\3\2\2\2\u013c;\3\2\2\2"+
		"\u013d\u013b\3\2\2\2\u013e\u0149\7\16\2\2\u013f\u0141\7\60\2\2\u0140\u0142"+
		"\7\61\2\2\u0141\u0140\3\2\2\2\u0141\u0142\3\2\2\2\u0142\u0144\3\2\2\2"+
		"\u0143\u0145\7\62\2\2\u0144\u0143\3\2\2\2\u0144\u0145\3\2\2\2\u0145\u0149"+
		"\3\2\2\2\u0146\u0149\7\r\2\2\u0147\u0149\5> \2\u0148\u013e\3\2\2\2\u0148"+
		"\u013f\3\2\2\2\u0148\u0146\3\2\2\2\u0148\u0147\3\2\2\2\u0149=\3\2\2\2"+
		"\u014a\u014b\t\n\2\2\u014b?\3\2\2\2\u014c\u014f\5B\"\2\u014d\u014f\5D"+
		"#\2\u014e\u014c\3\2\2\2\u014e\u014d\3\2\2\2\u014fA\3\2\2\2\u0150\u0151"+
		"\t\13\2\2\u0151C\3\2\2\2\u0152\u0153\7 \2\2\u0153\u0154\7\60\2\2\u0154"+
		"\u0155\7#\2\2\u0155\u0156\7\60\2\2\u0156\u0157\7!\2\2\u0157E\3\2\2\2\u0158"+
		"\u0159\t\f\2\2\u0159G\3\2\2\2\"RXjsvy\u0093\u009a\u00a2\u00a6\u00a9\u00af"+
		"\u00b7\u00be\u00c7\u00cc\u00d2\u00d7\u00dc\u00df\u00ec\u00f7\u0108\u010e"+
		"\u011f\u0121\u012b\u013b\u0141\u0144\u0148\u014e";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}