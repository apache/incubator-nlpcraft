// Generated from /Users/nivanov/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/internal/intent/compiler/antlr4/NCIDL.g4 by ANTLR 4.9.2
package org.apache.nlpcraft.internal.intent.compiler.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

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
		RULE_intentId = 9, RULE_mtdDecl = 10, RULE_flowDecl = 11, RULE_metaDecl = 12, 
		RULE_optDecl = 13, RULE_jsonObj = 14, RULE_jsonPair = 15, RULE_jsonVal = 16, 
		RULE_jsonArr = 17, RULE_termDecls = 18, RULE_termDecl = 19, RULE_termEq = 20, 
		RULE_term = 21, RULE_mtdRef = 22, RULE_javaFqn = 23, RULE_javaClass = 24, 
		RULE_termId = 25, RULE_expr = 26, RULE_vars = 27, RULE_varDecl = 28, RULE_paramList = 29, 
		RULE_atom = 30, RULE_qstring = 31, RULE_minMax = 32, RULE_minMaxShortcut = 33, 
		RULE_minMaxRange = 34, RULE_id = 35;
	private static String[] makeRuleNames() {
		return new String[] {
			"idl", "idlDecls", "idlDecl", "imprt", "frag", "fragId", "fragRef", "fragMeta", 
			"intent", "intentId", "mtdDecl", "flowDecl", "metaDecl", "optDecl", "jsonObj", 
			"jsonPair", "jsonVal", "jsonArr", "termDecls", "termDecl", "termEq", 
			"term", "mtdRef", "javaFqn", "javaClass", "termId", "expr", "vars", "varDecl", 
			"paramList", "atom", "qstring", "minMax", "minMaxShortcut", "minMaxRange", 
			"id"
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
			setState(72);
			idlDecls(0);
			setState(73);
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
			setState(76);
			idlDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(82);
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
					setState(78);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(79);
					idlDecl();
					}
					} 
				}
				setState(84);
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
			setState(88);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(85);
				intent();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
				frag();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 3);
				{
				setState(87);
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
			setState(90);
			match(IMPORT);
			setState(91);
			match(LPAR);
			setState(92);
			qstring();
			setState(93);
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
			setState(95);
			fragId();
			setState(96);
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
			setState(98);
			match(FRAG);
			setState(99);
			match(ASSIGN);
			setState(100);
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
			setState(102);
			match(FRAG);
			setState(103);
			match(LPAR);
			setState(104);
			id();
			setState(106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(105);
				fragMeta();
				}
			}

			setState(108);
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
			setState(110);
			match(COMMA);
			setState(111);
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
			setState(113);
			intentId();
			setState(115);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==OPTIONS) {
				{
				setState(114);
				optDecl();
				}
			}

			setState(118);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FLOW) {
				{
				setState(117);
				flowDecl();
				}
			}

			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==META) {
				{
				setState(120);
				metaDecl();
				}
			}

			setState(123);
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
			setState(125);
			match(INTENT);
			setState(126);
			match(ASSIGN);
			setState(127);
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

	public static class MtdDeclContext extends ParserRuleContext {
		public List<TerminalNode> DIV() { return getTokens(NCIDLParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(NCIDLParser.DIV, i);
		}
		public MtdRefContext mtdRef() {
			return getRuleContext(MtdRefContext.class,0);
		}
		public MtdDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mtdDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).enterMtdDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIDLListener ) ((NCIDLListener)listener).exitMtdDecl(this);
		}
	}

	public final MtdDeclContext mtdDecl() throws RecognitionException {
		MtdDeclContext _localctx = new MtdDeclContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_mtdDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			match(DIV);
			setState(130);
			mtdRef();
			setState(131);
			match(DIV);
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
		public MtdDeclContext mtdDecl() {
			return getRuleContext(MtdDeclContext.class,0);
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
		enterRule(_localctx, 22, RULE_flowDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(133);
			match(FLOW);
			setState(134);
			match(ASSIGN);
			setState(137);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				{
				setState(135);
				qstring();
				}
				break;
			case DIV:
				{
				setState(136);
				mtdDecl();
				}
				break;
			default:
				throw new NoViableAltException(this);
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
		enterRule(_localctx, 24, RULE_metaDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(139);
			match(META);
			setState(140);
			match(ASSIGN);
			setState(141);
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
		enterRule(_localctx, 26, RULE_optDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(143);
			match(OPTIONS);
			setState(144);
			match(ASSIGN);
			setState(145);
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
		enterRule(_localctx, 28, RULE_jsonObj);
		int _la;
		try {
			setState(160);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(147);
				match(LBRACE);
				setState(148);
				jsonPair();
				setState(153);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(149);
					match(COMMA);
					setState(150);
					jsonPair();
					}
					}
					setState(155);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(156);
				match(RBRACE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(158);
				match(LBRACE);
				setState(159);
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
		enterRule(_localctx, 30, RULE_jsonPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			qstring();
			setState(163);
			match(COLON);
			setState(164);
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
		enterRule(_localctx, 32, RULE_jsonVal);
		int _la;
		try {
			setState(181);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(166);
				qstring();
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(168);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(167);
					match(MINUS);
					}
				}

				setState(170);
				match(INT);
				setState(172);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REAL) {
					{
					setState(171);
					match(REAL);
					}
				}

				setState(175);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXP) {
					{
					setState(174);
					match(EXP);
					}
				}

				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(177);
				jsonObj();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 4);
				{
				setState(178);
				jsonArr();
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 5);
				{
				setState(179);
				match(BOOL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(180);
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
		enterRule(_localctx, 34, RULE_jsonArr);
		int _la;
		try {
			setState(196);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				match(LBR);
				setState(184);
				jsonVal();
				setState(189);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(185);
					match(COMMA);
					setState(186);
					jsonVal();
					}
					}
					setState(191);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(192);
				match(RBR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(194);
				match(LBR);
				setState(195);
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
		int _startState = 36;
		enterRecursionRule(_localctx, 36, RULE_termDecls, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(199);
			termDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(205);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermDeclsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_termDecls);
					setState(201);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(202);
					termDecl();
					}
					} 
				}
				setState(207);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
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
		enterRule(_localctx, 38, RULE_termDecl);
		try {
			setState(210);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(208);
				term();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(209);
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
		enterRule(_localctx, 40, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(212);
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
		public MtdDeclContext mtdDecl() {
			return getRuleContext(MtdDeclContext.class,0);
		}
		public TermIdContext termId() {
			return getRuleContext(TermIdContext.class,0);
		}
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public TerminalNode LBRACE() { return getToken(NCIDLParser.LBRACE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(NCIDLParser.RBRACE, 0); }
		public VarsContext vars() {
			return getRuleContext(VarsContext.class,0);
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
		enterRule(_localctx, 42, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(214);
			match(TERM);
			setState(216);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAR) {
				{
				setState(215);
				termId();
				}
			}

			setState(218);
			termEq();
			setState(227);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				{
				{
				setState(219);
				match(LBRACE);
				setState(221);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(220);
					vars(0);
					}
					break;
				}
				setState(223);
				expr(0);
				setState(224);
				match(RBRACE);
				}
				}
				break;
			case DIV:
				{
				setState(226);
				mtdDecl();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(230);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				{
				setState(229);
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
		enterRule(_localctx, 44, RULE_mtdRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << IMPORT) | (1L << INTENT) | (1L << OPTIONS) | (1L << FLOW) | (1L << META) | (1L << TERM) | (1L << FRAG) | (1L << ID))) != 0)) {
				{
				setState(232);
				javaFqn(0);
				}
			}

			setState(235);
			match(POUND);
			setState(236);
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
		int _startState = 46;
		enterRecursionRule(_localctx, 46, RULE_javaFqn, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(239);
			javaClass();
			}
			_ctx.stop = _input.LT(-1);
			setState(246);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,22,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new JavaFqnContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_javaFqn);
					setState(241);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(242);
					match(DOT);
					setState(243);
					javaClass();
					}
					} 
				}
				setState(248);
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
		enterRule(_localctx, 48, RULE_javaClass);
		try {
			setState(257);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FUN_NAME:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(249);
				id();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 2);
				{
				setState(250);
				match(IMPORT);
				}
				break;
			case INTENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(251);
				match(INTENT);
				}
				break;
			case OPTIONS:
				enterOuterAlt(_localctx, 4);
				{
				setState(252);
				match(OPTIONS);
				}
				break;
			case FLOW:
				enterOuterAlt(_localctx, 5);
				{
				setState(253);
				match(FLOW);
				}
				break;
			case META:
				enterOuterAlt(_localctx, 6);
				{
				setState(254);
				match(META);
				}
				break;
			case TERM:
				enterOuterAlt(_localctx, 7);
				{
				setState(255);
				match(TERM);
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 8);
				{
				setState(256);
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
		enterRule(_localctx, 50, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			match(LPAR);
			setState(260);
			id();
			setState(261);
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
		int _startState = 52;
		enterRecursionRule(_localctx, 52, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(280);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(264);
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
				setState(265);
				expr(11);
				}
				break;
			case 2:
				{
				_localctx = new ParExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(266);
				match(LPAR);
				setState(267);
				expr(0);
				setState(268);
				match(RPAR);
				}
				break;
			case 3:
				{
				_localctx = new AtomExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(270);
				atom();
				}
				break;
			case 4:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(271);
				_la = _input.LA(1);
				if ( !(_la==FUN_NAME || _la==POUND) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(272);
				match(LPAR);
				setState(274);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << SQSTRING) | (1L << DQSTRING) | (1L << BOOL) | (1L << NULL) | (1L << NOT) | (1L << LPAR) | (1L << POUND) | (1L << MINUS) | (1L << AT) | (1L << INT))) != 0)) {
					{
					setState(273);
					paramList(0);
					}
				}

				setState(276);
				match(RPAR);
				}
				break;
			case 5:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(277);
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
				setState(278);
				match(AT);
				setState(279);
				id();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(299);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(297);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
					case 1:
						{
						_localctx = new MultDivModExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(282);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(283);
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
						setState(284);
						expr(10);
						}
						break;
					case 2:
						{
						_localctx = new PlusMinusExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(285);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(286);
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
						setState(287);
						expr(9);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(288);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(289);
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
						setState(290);
						expr(8);
						}
						break;
					case 4:
						{
						_localctx = new EqNeqExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(291);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(292);
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
						setState(293);
						expr(7);
						}
						break;
					case 5:
						{
						_localctx = new AndOrExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(294);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(295);
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
						setState(296);
						expr(6);
						}
						break;
					}
					} 
				}
				setState(301);
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
		int _startState = 54;
		enterRecursionRule(_localctx, 54, RULE_vars, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(303);
			varDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(309);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new VarsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_vars);
					setState(305);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(306);
					varDecl();
					}
					} 
				}
				setState(311);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
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
		enterRule(_localctx, 56, RULE_varDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(312);
			match(AT);
			setState(313);
			id();
			setState(314);
			match(ASSIGN);
			setState(315);
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
		int _startState = 58;
		enterRecursionRule(_localctx, 58, RULE_paramList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(318);
			expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(325);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ParamListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_paramList);
					setState(320);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(321);
					match(COMMA);
					setState(322);
					expr(0);
					}
					} 
				}
				setState(327);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
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
		enterRule(_localctx, 60, RULE_atom);
		try {
			setState(338);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(328);
				match(NULL);
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(329);
				match(INT);
				setState(331);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,30,_ctx) ) {
				case 1:
					{
					setState(330);
					match(REAL);
					}
					break;
				}
				setState(334);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(333);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(336);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(337);
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
		enterRule(_localctx, 62, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(340);
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
		enterRule(_localctx, 64, RULE_minMax);
		try {
			setState(344);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case MULT:
				enterOuterAlt(_localctx, 1);
				{
				setState(342);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(343);
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
		enterRule(_localctx, 66, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
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
		enterRule(_localctx, 68, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(348);
			match(LBR);
			setState(349);
			match(INT);
			setState(350);
			match(COMMA);
			setState(351);
			match(INT);
			setState(352);
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
		enterRule(_localctx, 70, RULE_id);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
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
		case 18:
			return termDecls_sempred((TermDeclsContext)_localctx, predIndex);
		case 23:
			return javaFqn_sempred((JavaFqnContext)_localctx, predIndex);
		case 26:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 27:
			return vars_sempred((VarsContext)_localctx, predIndex);
		case 29:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\66\u0167\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\3\7\3S\n\3"+
		"\f\3\16\3V\13\3\3\4\3\4\3\4\5\4[\n\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3"+
		"\7\3\7\3\7\3\7\3\b\3\b\3\b\3\b\5\bm\n\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\5"+
		"\nv\n\n\3\n\5\ny\n\n\3\n\5\n|\n\n\3\n\3\n\3\13\3\13\3\13\3\13\3\f\3\f"+
		"\3\f\3\f\3\r\3\r\3\r\3\r\5\r\u008c\n\r\3\16\3\16\3\16\3\16\3\17\3\17\3"+
		"\17\3\17\3\20\3\20\3\20\3\20\7\20\u009a\n\20\f\20\16\20\u009d\13\20\3"+
		"\20\3\20\3\20\3\20\5\20\u00a3\n\20\3\21\3\21\3\21\3\21\3\22\3\22\5\22"+
		"\u00ab\n\22\3\22\3\22\5\22\u00af\n\22\3\22\5\22\u00b2\n\22\3\22\3\22\3"+
		"\22\3\22\5\22\u00b8\n\22\3\23\3\23\3\23\3\23\7\23\u00be\n\23\f\23\16\23"+
		"\u00c1\13\23\3\23\3\23\3\23\3\23\5\23\u00c7\n\23\3\24\3\24\3\24\3\24\3"+
		"\24\7\24\u00ce\n\24\f\24\16\24\u00d1\13\24\3\25\3\25\5\25\u00d5\n\25\3"+
		"\26\3\26\3\27\3\27\5\27\u00db\n\27\3\27\3\27\3\27\5\27\u00e0\n\27\3\27"+
		"\3\27\3\27\3\27\5\27\u00e6\n\27\3\27\5\27\u00e9\n\27\3\30\5\30\u00ec\n"+
		"\30\3\30\3\30\3\30\3\31\3\31\3\31\3\31\3\31\3\31\7\31\u00f7\n\31\f\31"+
		"\16\31\u00fa\13\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\3\32\5\32\u0104"+
		"\n\32\3\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34"+
		"\3\34\3\34\5\34\u0115\n\34\3\34\3\34\3\34\3\34\5\34\u011b\n\34\3\34\3"+
		"\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\7"+
		"\34\u012c\n\34\f\34\16\34\u012f\13\34\3\35\3\35\3\35\3\35\3\35\7\35\u0136"+
		"\n\35\f\35\16\35\u0139\13\35\3\36\3\36\3\36\3\36\3\36\3\37\3\37\3\37\3"+
		"\37\3\37\3\37\7\37\u0146\n\37\f\37\16\37\u0149\13\37\3 \3 \3 \5 \u014e"+
		"\n \3 \5 \u0151\n \3 \3 \5 \u0155\n \3!\3!\3\"\3\"\5\"\u015b\n\"\3#\3"+
		"#\3$\3$\3$\3$\3$\3$\3%\3%\3%\2\b\4&\60\668<&\2\4\6\b\n\f\16\20\22\24\26"+
		"\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFH\2\r\4\2\37\37((\4\2\30\30%"+
		"%\4\2\3\3\"\"\3\2+-\4\2%%))\3\2\21\24\3\2\17\20\3\2\25\26\3\2\13\f\3\2"+
		")+\4\2\3\3\63\63\2\u0178\2J\3\2\2\2\4M\3\2\2\2\6Z\3\2\2\2\b\\\3\2\2\2"+
		"\na\3\2\2\2\fd\3\2\2\2\16h\3\2\2\2\20p\3\2\2\2\22s\3\2\2\2\24\177\3\2"+
		"\2\2\26\u0083\3\2\2\2\30\u0087\3\2\2\2\32\u008d\3\2\2\2\34\u0091\3\2\2"+
		"\2\36\u00a2\3\2\2\2 \u00a4\3\2\2\2\"\u00b7\3\2\2\2$\u00c6\3\2\2\2&\u00c8"+
		"\3\2\2\2(\u00d4\3\2\2\2*\u00d6\3\2\2\2,\u00d8\3\2\2\2.\u00eb\3\2\2\2\60"+
		"\u00f0\3\2\2\2\62\u0103\3\2\2\2\64\u0105\3\2\2\2\66\u011a\3\2\2\28\u0130"+
		"\3\2\2\2:\u013a\3\2\2\2<\u013f\3\2\2\2>\u0154\3\2\2\2@\u0156\3\2\2\2B"+
		"\u015a\3\2\2\2D\u015c\3\2\2\2F\u015e\3\2\2\2H\u0164\3\2\2\2JK\5\4\3\2"+
		"KL\7\2\2\3L\3\3\2\2\2MN\b\3\1\2NO\5\6\4\2OT\3\2\2\2PQ\f\3\2\2QS\5\6\4"+
		"\2RP\3\2\2\2SV\3\2\2\2TR\3\2\2\2TU\3\2\2\2U\5\3\2\2\2VT\3\2\2\2W[\5\22"+
		"\n\2X[\5\n\6\2Y[\5\b\5\2ZW\3\2\2\2ZX\3\2\2\2ZY\3\2\2\2[\7\3\2\2\2\\]\7"+
		"\4\2\2]^\7\31\2\2^_\5@!\2_`\7\32\2\2`\t\3\2\2\2ab\5\f\7\2bc\5&\24\2c\13"+
		"\3\2\2\2de\7\n\2\2ef\7(\2\2fg\5H%\2g\r\3\2\2\2hi\7\n\2\2ij\7\31\2\2jl"+
		"\5H%\2km\5\20\t\2lk\3\2\2\2lm\3\2\2\2mn\3\2\2\2no\7\32\2\2o\17\3\2\2\2"+
		"pq\7#\2\2qr\5\36\20\2r\21\3\2\2\2su\5\24\13\2tv\5\34\17\2ut\3\2\2\2uv"+
		"\3\2\2\2vx\3\2\2\2wy\5\30\r\2xw\3\2\2\2xy\3\2\2\2y{\3\2\2\2z|\5\32\16"+
		"\2{z\3\2\2\2{|\3\2\2\2|}\3\2\2\2}~\5&\24\2~\23\3\2\2\2\177\u0080\7\5\2"+
		"\2\u0080\u0081\7(\2\2\u0081\u0082\5H%\2\u0082\25\3\2\2\2\u0083\u0084\7"+
		",\2\2\u0084\u0085\5.\30\2\u0085\u0086\7,\2\2\u0086\27\3\2\2\2\u0087\u0088"+
		"\7\7\2\2\u0088\u008b\7(\2\2\u0089\u008c\5@!\2\u008a\u008c\5\26\f\2\u008b"+
		"\u0089\3\2\2\2\u008b\u008a\3\2\2\2\u008c\31\3\2\2\2\u008d\u008e\7\b\2"+
		"\2\u008e\u008f\7(\2\2\u008f\u0090\5\36\20\2\u0090\33\3\2\2\2\u0091\u0092"+
		"\7\6\2\2\u0092\u0093\7(\2\2\u0093\u0094\5\36\20\2\u0094\35\3\2\2\2\u0095"+
		"\u0096\7\33\2\2\u0096\u009b\5 \21\2\u0097\u0098\7#\2\2\u0098\u009a\5 "+
		"\21\2\u0099\u0097\3\2\2\2\u009a\u009d\3\2\2\2\u009b\u0099\3\2\2\2\u009b"+
		"\u009c\3\2\2\2\u009c\u009e\3\2\2\2\u009d\u009b\3\2\2\2\u009e\u009f\7\34"+
		"\2\2\u009f\u00a3\3\2\2\2\u00a0\u00a1\7\33\2\2\u00a1\u00a3\7\34\2\2\u00a2"+
		"\u0095\3\2\2\2\u00a2\u00a0\3\2\2\2\u00a3\37\3\2\2\2\u00a4\u00a5\5@!\2"+
		"\u00a5\u00a6\7$\2\2\u00a6\u00a7\5\"\22\2\u00a7!\3\2\2\2\u00a8\u00b8\5"+
		"@!\2\u00a9\u00ab\7%\2\2\u00aa\u00a9\3\2\2\2\u00aa\u00ab\3\2\2\2\u00ab"+
		"\u00ac\3\2\2\2\u00ac\u00ae\7\60\2\2\u00ad\u00af\7\61\2\2\u00ae\u00ad\3"+
		"\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b1\3\2\2\2\u00b0\u00b2\7\62\2\2\u00b1"+
		"\u00b0\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b8\3\2\2\2\u00b3\u00b8\5\36"+
		"\20\2\u00b4\u00b8\5$\23\2\u00b5\u00b8\7\r\2\2\u00b6\u00b8\7\16\2\2\u00b7"+
		"\u00a8\3\2\2\2\u00b7\u00aa\3\2\2\2\u00b7\u00b3\3\2\2\2\u00b7\u00b4\3\2"+
		"\2\2\u00b7\u00b5\3\2\2\2\u00b7\u00b6\3\2\2\2\u00b8#\3\2\2\2\u00b9\u00ba"+
		"\7 \2\2\u00ba\u00bf\5\"\22\2\u00bb\u00bc\7#\2\2\u00bc\u00be\5\"\22\2\u00bd"+
		"\u00bb\3\2\2\2\u00be\u00c1\3\2\2\2\u00bf\u00bd\3\2\2\2\u00bf\u00c0\3\2"+
		"\2\2\u00c0\u00c2\3\2\2\2\u00c1\u00bf\3\2\2\2\u00c2\u00c3\7!\2\2\u00c3"+
		"\u00c7\3\2\2\2\u00c4\u00c5\7 \2\2\u00c5\u00c7\7!\2\2\u00c6\u00b9\3\2\2"+
		"\2\u00c6\u00c4\3\2\2\2\u00c7%\3\2\2\2\u00c8\u00c9\b\24\1\2\u00c9\u00ca"+
		"\5(\25\2\u00ca\u00cf\3\2\2\2\u00cb\u00cc\f\3\2\2\u00cc\u00ce\5(\25\2\u00cd"+
		"\u00cb\3\2\2\2\u00ce\u00d1\3\2\2\2\u00cf\u00cd\3\2\2\2\u00cf\u00d0\3\2"+
		"\2\2\u00d0\'\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d2\u00d5\5,\27\2\u00d3\u00d5"+
		"\5\16\b\2\u00d4\u00d2\3\2\2\2\u00d4\u00d3\3\2\2\2\u00d5)\3\2\2\2\u00d6"+
		"\u00d7\t\2\2\2\u00d7+\3\2\2\2\u00d8\u00da\7\t\2\2\u00d9\u00db\5\64\33"+
		"\2\u00da\u00d9\3\2\2\2\u00da\u00db\3\2\2\2\u00db\u00dc\3\2\2\2\u00dc\u00e5"+
		"\5*\26\2\u00dd\u00df\7\33\2\2\u00de\u00e0\58\35\2\u00df\u00de\3\2\2\2"+
		"\u00df\u00e0\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e2\5\66\34\2\u00e2\u00e3"+
		"\7\34\2\2\u00e3\u00e6\3\2\2\2\u00e4\u00e6\5\26\f\2\u00e5\u00dd\3\2\2\2"+
		"\u00e5\u00e4\3\2\2\2\u00e6\u00e8\3\2\2\2\u00e7\u00e9\5B\"\2\u00e8\u00e7"+
		"\3\2\2\2\u00e8\u00e9\3\2\2\2\u00e9-\3\2\2\2\u00ea\u00ec\5\60\31\2\u00eb"+
		"\u00ea\3\2\2\2\u00eb\u00ec\3\2\2\2\u00ec\u00ed\3\2\2\2\u00ed\u00ee\7\""+
		"\2\2\u00ee\u00ef\5H%\2\u00ef/\3\2\2\2\u00f0\u00f1\b\31\1\2\u00f1\u00f2"+
		"\5\62\32\2\u00f2\u00f8\3\2\2\2\u00f3\u00f4\f\3\2\2\u00f4\u00f5\7&\2\2"+
		"\u00f5\u00f7\5\62\32\2\u00f6\u00f3\3\2\2\2\u00f7\u00fa\3\2\2\2\u00f8\u00f6"+
		"\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\61\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fb"+
		"\u0104\5H%\2\u00fc\u0104\7\4\2\2\u00fd\u0104\7\5\2\2\u00fe\u0104\7\6\2"+
		"\2\u00ff\u0104\7\7\2\2\u0100\u0104\7\b\2\2\u0101\u0104\7\t\2\2\u0102\u0104"+
		"\7\n\2\2\u0103\u00fb\3\2\2\2\u0103\u00fc\3\2\2\2\u0103\u00fd\3\2\2\2\u0103"+
		"\u00fe\3\2\2\2\u0103\u00ff\3\2\2\2\u0103\u0100\3\2\2\2\u0103\u0101\3\2"+
		"\2\2\u0103\u0102\3\2\2\2\u0104\63\3\2\2\2\u0105\u0106\7\31\2\2\u0106\u0107"+
		"\5H%\2\u0107\u0108\7\32\2\2\u0108\65\3\2\2\2\u0109\u010a\b\34\1\2\u010a"+
		"\u010b\t\3\2\2\u010b\u011b\5\66\34\r\u010c\u010d\7\31\2\2\u010d\u010e"+
		"\5\66\34\2\u010e\u010f\7\32\2\2\u010f\u011b\3\2\2\2\u0110\u011b\5> \2"+
		"\u0111\u0112\t\4\2\2\u0112\u0114\7\31\2\2\u0113\u0115\5<\37\2\u0114\u0113"+
		"\3\2\2\2\u0114\u0115\3\2\2\2\u0115\u0116\3\2\2\2\u0116\u011b\7\32\2\2"+
		"\u0117\u011b\t\4\2\2\u0118\u0119\7.\2\2\u0119\u011b\5H%\2\u011a\u0109"+
		"\3\2\2\2\u011a\u010c\3\2\2\2\u011a\u0110\3\2\2\2\u011a\u0111\3\2\2\2\u011a"+
		"\u0117\3\2\2\2\u011a\u0118\3\2\2\2\u011b\u012d\3\2\2\2\u011c\u011d\f\13"+
		"\2\2\u011d\u011e\t\5\2\2\u011e\u012c\5\66\34\f\u011f\u0120\f\n\2\2\u0120"+
		"\u0121\t\6\2\2\u0121\u012c\5\66\34\13\u0122\u0123\f\t\2\2\u0123\u0124"+
		"\t\7\2\2\u0124\u012c\5\66\34\n\u0125\u0126\f\b\2\2\u0126\u0127\t\b\2\2"+
		"\u0127\u012c\5\66\34\t\u0128\u0129\f\7\2\2\u0129\u012a\t\t\2\2\u012a\u012c"+
		"\5\66\34\b\u012b\u011c\3\2\2\2\u012b\u011f\3\2\2\2\u012b\u0122\3\2\2\2"+
		"\u012b\u0125\3\2\2\2\u012b\u0128\3\2\2\2\u012c\u012f\3\2\2\2\u012d\u012b"+
		"\3\2\2\2\u012d\u012e\3\2\2\2\u012e\67\3\2\2\2\u012f\u012d\3\2\2\2\u0130"+
		"\u0131\b\35\1\2\u0131\u0132\5:\36\2\u0132\u0137\3\2\2\2\u0133\u0134\f"+
		"\3\2\2\u0134\u0136\5:\36\2\u0135\u0133\3\2\2\2\u0136\u0139\3\2\2\2\u0137"+
		"\u0135\3\2\2\2\u0137\u0138\3\2\2\2\u01389\3\2\2\2\u0139\u0137\3\2\2\2"+
		"\u013a\u013b\7.\2\2\u013b\u013c\5H%\2\u013c\u013d\7(\2\2\u013d\u013e\5"+
		"\66\34\2\u013e;\3\2\2\2\u013f\u0140\b\37\1\2\u0140\u0141\5\66\34\2\u0141"+
		"\u0147\3\2\2\2\u0142\u0143\f\3\2\2\u0143\u0144\7#\2\2\u0144\u0146\5\66"+
		"\34\2\u0145\u0142\3\2\2\2\u0146\u0149\3\2\2\2\u0147\u0145\3\2\2\2\u0147"+
		"\u0148\3\2\2\2\u0148=\3\2\2\2\u0149\u0147\3\2\2\2\u014a\u0155\7\16\2\2"+
		"\u014b\u014d\7\60\2\2\u014c\u014e\7\61\2\2\u014d\u014c\3\2\2\2\u014d\u014e"+
		"\3\2\2\2\u014e\u0150\3\2\2\2\u014f\u0151\7\62\2\2\u0150\u014f\3\2\2\2"+
		"\u0150\u0151\3\2\2\2\u0151\u0155\3\2\2\2\u0152\u0155\7\r\2\2\u0153\u0155"+
		"\5@!\2\u0154\u014a\3\2\2\2\u0154\u014b\3\2\2\2\u0154\u0152\3\2\2\2\u0154"+
		"\u0153\3\2\2\2\u0155?\3\2\2\2\u0156\u0157\t\n\2\2\u0157A\3\2\2\2\u0158"+
		"\u015b\5D#\2\u0159\u015b\5F$\2\u015a\u0158\3\2\2\2\u015a\u0159\3\2\2\2"+
		"\u015bC\3\2\2\2\u015c\u015d\t\13\2\2\u015dE\3\2\2\2\u015e\u015f\7 \2\2"+
		"\u015f\u0160\7\60\2\2\u0160\u0161\7#\2\2\u0161\u0162\7\60\2\2\u0162\u0163"+
		"\7!\2\2\u0163G\3\2\2\2\u0164\u0165\t\f\2\2\u0165I\3\2\2\2$TZlux{\u008b"+
		"\u009b\u00a2\u00aa\u00ae\u00b1\u00b7\u00bf\u00c6\u00cf\u00d4\u00da\u00df"+
		"\u00e5\u00e8\u00eb\u00f8\u0103\u0114\u011a\u012b\u012d\u0137\u0147\u014d"+
		"\u0150\u0154\u015a";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}