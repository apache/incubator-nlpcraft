// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/compiler/antlr4\NCIdl.g4 by ANTLR 4.9.1
package org.apache.nlpcraft.model.intent.compiler.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIdlParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		FUN_NAME=1, IMPORT=2, INTENT=3, ORDERED=4, FLOW=5, META=6, TERM=7, FRAG=8, 
		SQSTRING=9, DQSTRING=10, BOOL=11, NULL=12, EQ=13, NEQ=14, GTEQ=15, LTEQ=16, 
		GT=17, LT=18, AND=19, OR=20, VERT=21, NOT=22, LPAR=23, RPAR=24, LBRACE=25, 
		RBRACE=26, SQUOTE=27, DQUOTE=28, TILDA=29, LBR=30, RBR=31, POUND=32, COMMA=33, 
		COLON=34, MINUS=35, DOT=36, UNDERSCORE=37, ASSIGN=38, PLUS=39, QUESTION=40, 
		MULT=41, DIV=42, MOD=43, AT=44, DOLLAR=45, INT=46, REAL=47, EXP=48, ID=49, 
		COMMENT=50, WS=51, ErrorChar=52;
	public static final int
		RULE_idl = 0, RULE_synonym = 1, RULE_alias = 2, RULE_idlDecls = 3, RULE_idlDecl = 4, 
		RULE_imp = 5, RULE_frag = 6, RULE_fragId = 7, RULE_fragRef = 8, RULE_fragMeta = 9, 
		RULE_intent = 10, RULE_intentId = 11, RULE_orderedDecl = 12, RULE_mtdDecl = 13, 
		RULE_flowDecl = 14, RULE_metaDecl = 15, RULE_jsonObj = 16, RULE_jsonPair = 17, 
		RULE_jsonVal = 18, RULE_jsonArr = 19, RULE_termDecls = 20, RULE_termDecl = 21, 
		RULE_termEq = 22, RULE_term = 23, RULE_mtdRef = 24, RULE_javaFqn = 25, 
		RULE_termId = 26, RULE_expr = 27, RULE_vars = 28, RULE_varDecl = 29, RULE_paramList = 30, 
		RULE_atom = 31, RULE_qstring = 32, RULE_minMax = 33, RULE_minMaxShortcut = 34, 
		RULE_minMaxRange = 35, RULE_id = 36;
	private static String[] makeRuleNames() {
		return new String[] {
			"idl", "synonym", "alias", "idlDecls", "idlDecl", "imp", "frag", "fragId", 
			"fragRef", "fragMeta", "intent", "intentId", "orderedDecl", "mtdDecl", 
			"flowDecl", "metaDecl", "jsonObj", "jsonPair", "jsonVal", "jsonArr", 
			"termDecls", "termDecl", "termEq", "term", "mtdRef", "javaFqn", "termId", 
			"expr", "vars", "varDecl", "paramList", "atom", "qstring", "minMax", 
			"minMaxShortcut", "minMaxRange", "id"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, null, "'import'", "'intent'", "'ordered'", "'flow'", "'meta'", 
			"'term'", "'fragment'", null, null, null, "'null'", "'=='", "'!='", "'>='", 
			"'<='", "'>'", "'<'", "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", 
			"'}'", "'''", "'\"'", "'~'", "'['", "']'", "'#'", "','", "':'", "'-'", 
			"'.'", "'_'", "'='", "'+'", "'?'", "'*'", "'/'", "'%'", "'@'", "'$'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, "FUN_NAME", "IMPORT", "INTENT", "ORDERED", "FLOW", "META", "TERM", 
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
	public String getGrammarFileName() { return "NCIdl.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public NCIdlParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class IdlContext extends ParserRuleContext {
		public IdlDeclsContext idlDecls() {
			return getRuleContext(IdlDeclsContext.class,0);
		}
		public TerminalNode EOF() { return getToken(NCIdlParser.EOF, 0); }
		public IdlContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterIdl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitIdl(this);
		}
	}

	public final IdlContext idl() throws RecognitionException {
		IdlContext _localctx = new IdlContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_idl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
			idlDecls(0);
			setState(75);
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

	public static class SynonymContext extends ParserRuleContext {
		public TerminalNode LBRACE() { return getToken(NCIdlParser.LBRACE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(NCIdlParser.RBRACE, 0); }
		public TerminalNode EOF() { return getToken(NCIdlParser.EOF, 0); }
		public AliasContext alias() {
			return getRuleContext(AliasContext.class,0);
		}
		public VarsContext vars() {
			return getRuleContext(VarsContext.class,0);
		}
		public SynonymContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_synonym; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterSynonym(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitSynonym(this);
		}
	}

	public final SynonymContext synonym() throws RecognitionException {
		SynonymContext _localctx = new SynonymContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_synonym);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBR) {
				{
				setState(77);
				alias();
				}
			}

			setState(80);
			match(LBRACE);
			setState(82);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(81);
				vars(0);
				}
				break;
			}
			setState(84);
			expr(0);
			setState(85);
			match(RBRACE);
			setState(86);
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

	public static class AliasContext extends ParserRuleContext {
		public TerminalNode LBR() { return getToken(NCIdlParser.LBR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode RBR() { return getToken(NCIdlParser.RBR, 0); }
		public AliasContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_alias; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterAlias(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitAlias(this);
		}
	}

	public final AliasContext alias() throws RecognitionException {
		AliasContext _localctx = new AliasContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_alias);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(88);
			match(LBR);
			setState(89);
			id();
			setState(90);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterIdlDecls(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitIdlDecls(this);
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
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_idlDecls, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(93);
			idlDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(99);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new IdlDeclsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_idlDecls);
					setState(95);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(96);
					idlDecl();
					}
					} 
				}
				setState(101);
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

	public static class IdlDeclContext extends ParserRuleContext {
		public IntentContext intent() {
			return getRuleContext(IntentContext.class,0);
		}
		public FragContext frag() {
			return getRuleContext(FragContext.class,0);
		}
		public ImpContext imp() {
			return getRuleContext(ImpContext.class,0);
		}
		public IdlDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idlDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterIdlDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitIdlDecl(this);
		}
	}

	public final IdlDeclContext idlDecl() throws RecognitionException {
		IdlDeclContext _localctx = new IdlDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_idlDecl);
		try {
			setState(105);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(102);
				intent();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(103);
				frag();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 3);
				{
				setState(104);
				imp();
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

	public static class ImpContext extends ParserRuleContext {
		public TerminalNode IMPORT() { return getToken(NCIdlParser.IMPORT, 0); }
		public TerminalNode LPAR() { return getToken(NCIdlParser.LPAR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIdlParser.RPAR, 0); }
		public ImpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_imp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterImp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitImp(this);
		}
	}

	public final ImpContext imp() throws RecognitionException {
		ImpContext _localctx = new ImpContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_imp);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(107);
			match(IMPORT);
			setState(108);
			match(LPAR);
			setState(109);
			qstring();
			setState(110);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterFrag(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitFrag(this);
		}
	}

	public final FragContext frag() throws RecognitionException {
		FragContext _localctx = new FragContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_frag);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112);
			fragId();
			setState(113);
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
		public TerminalNode FRAG() { return getToken(NCIdlParser.FRAG, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public FragIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterFragId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitFragId(this);
		}
	}

	public final FragIdContext fragId() throws RecognitionException {
		FragIdContext _localctx = new FragIdContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_fragId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(FRAG);
			setState(116);
			match(ASSIGN);
			setState(117);
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
		public TerminalNode FRAG() { return getToken(NCIdlParser.FRAG, 0); }
		public TerminalNode LPAR() { return getToken(NCIdlParser.LPAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIdlParser.RPAR, 0); }
		public FragMetaContext fragMeta() {
			return getRuleContext(FragMetaContext.class,0);
		}
		public FragRefContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragRef; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterFragRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitFragRef(this);
		}
	}

	public final FragRefContext fragRef() throws RecognitionException {
		FragRefContext _localctx = new FragRefContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_fragRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(FRAG);
			setState(120);
			match(LPAR);
			setState(121);
			id();
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(122);
				fragMeta();
				}
			}

			setState(125);
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
		public TerminalNode COMMA() { return getToken(NCIdlParser.COMMA, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public FragMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fragMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterFragMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitFragMeta(this);
		}
	}

	public final FragMetaContext fragMeta() throws RecognitionException {
		FragMetaContext _localctx = new FragMetaContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_fragMeta);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(127);
			match(COMMA);
			setState(128);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterIntent(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitIntent(this);
		}
	}

	public final IntentContext intent() throws RecognitionException {
		IntentContext _localctx = new IntentContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_intent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			intentId();
			setState(132);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDERED) {
				{
				setState(131);
				orderedDecl();
				}
			}

			setState(135);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FLOW) {
				{
				setState(134);
				flowDecl();
				}
			}

			setState(138);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==META) {
				{
				setState(137);
				metaDecl();
				}
			}

			setState(140);
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
		public TerminalNode INTENT() { return getToken(NCIdlParser.INTENT, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public IntentIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intentId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterIntentId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitIntentId(this);
		}
	}

	public final IntentIdContext intentId() throws RecognitionException {
		IntentIdContext _localctx = new IntentIdContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_intentId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			match(INTENT);
			setState(143);
			match(ASSIGN);
			setState(144);
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

	public static class OrderedDeclContext extends ParserRuleContext {
		public TerminalNode ORDERED() { return getToken(NCIdlParser.ORDERED, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
		public TerminalNode BOOL() { return getToken(NCIdlParser.BOOL, 0); }
		public OrderedDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_orderedDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterOrderedDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitOrderedDecl(this);
		}
	}

	public final OrderedDeclContext orderedDecl() throws RecognitionException {
		OrderedDeclContext _localctx = new OrderedDeclContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_orderedDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(ORDERED);
			setState(147);
			match(ASSIGN);
			setState(148);
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

	public static class MtdDeclContext extends ParserRuleContext {
		public List<TerminalNode> DIV() { return getTokens(NCIdlParser.DIV); }
		public TerminalNode DIV(int i) {
			return getToken(NCIdlParser.DIV, i);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMtdDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMtdDecl(this);
		}
	}

	public final MtdDeclContext mtdDecl() throws RecognitionException {
		MtdDeclContext _localctx = new MtdDeclContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_mtdDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(150);
			match(DIV);
			setState(151);
			mtdRef();
			setState(152);
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
		public TerminalNode FLOW() { return getToken(NCIdlParser.FLOW, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterFlowDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitFlowDecl(this);
		}
	}

	public final FlowDeclContext flowDecl() throws RecognitionException {
		FlowDeclContext _localctx = new FlowDeclContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_flowDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(FLOW);
			setState(155);
			match(ASSIGN);
			setState(158);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				{
				setState(156);
				qstring();
				}
				break;
			case DIV:
				{
				setState(157);
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
		public TerminalNode META() { return getToken(NCIdlParser.META, 0); }
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public MetaDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMetaDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMetaDecl(this);
		}
	}

	public final MetaDeclContext metaDecl() throws RecognitionException {
		MetaDeclContext _localctx = new MetaDeclContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_metaDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(160);
			match(META);
			setState(161);
			match(ASSIGN);
			setState(162);
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
		public TerminalNode LBRACE() { return getToken(NCIdlParser.LBRACE, 0); }
		public List<JsonPairContext> jsonPair() {
			return getRuleContexts(JsonPairContext.class);
		}
		public JsonPairContext jsonPair(int i) {
			return getRuleContext(JsonPairContext.class,i);
		}
		public TerminalNode RBRACE() { return getToken(NCIdlParser.RBRACE, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NCIdlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NCIdlParser.COMMA, i);
		}
		public JsonObjContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonObj; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterJsonObj(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitJsonObj(this);
		}
	}

	public final JsonObjContext jsonObj() throws RecognitionException {
		JsonObjContext _localctx = new JsonObjContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_jsonObj);
		int _la;
		try {
			setState(177);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(164);
				match(LBRACE);
				setState(165);
				jsonPair();
				setState(170);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(166);
					match(COMMA);
					setState(167);
					jsonPair();
					}
					}
					setState(172);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(173);
				match(RBRACE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(175);
				match(LBRACE);
				setState(176);
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
		public TerminalNode COLON() { return getToken(NCIdlParser.COLON, 0); }
		public JsonValContext jsonVal() {
			return getRuleContext(JsonValContext.class,0);
		}
		public JsonPairContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonPair; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterJsonPair(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitJsonPair(this);
		}
	}

	public final JsonPairContext jsonPair() throws RecognitionException {
		JsonPairContext _localctx = new JsonPairContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_jsonPair);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			qstring();
			setState(180);
			match(COLON);
			setState(181);
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
		public TerminalNode INT() { return getToken(NCIdlParser.INT, 0); }
		public TerminalNode MINUS() { return getToken(NCIdlParser.MINUS, 0); }
		public TerminalNode REAL() { return getToken(NCIdlParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIdlParser.EXP, 0); }
		public JsonObjContext jsonObj() {
			return getRuleContext(JsonObjContext.class,0);
		}
		public JsonArrContext jsonArr() {
			return getRuleContext(JsonArrContext.class,0);
		}
		public TerminalNode BOOL() { return getToken(NCIdlParser.BOOL, 0); }
		public TerminalNode NULL() { return getToken(NCIdlParser.NULL, 0); }
		public JsonValContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonVal; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterJsonVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitJsonVal(this);
		}
	}

	public final JsonValContext jsonVal() throws RecognitionException {
		JsonValContext _localctx = new JsonValContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_jsonVal);
		int _la;
		try {
			setState(198);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(183);
				qstring();
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(185);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(184);
					match(MINUS);
					}
				}

				setState(187);
				match(INT);
				setState(189);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REAL) {
					{
					setState(188);
					match(REAL);
					}
				}

				setState(192);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXP) {
					{
					setState(191);
					match(EXP);
					}
				}

				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(194);
				jsonObj();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 4);
				{
				setState(195);
				jsonArr();
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 5);
				{
				setState(196);
				match(BOOL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(197);
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
		public TerminalNode LBR() { return getToken(NCIdlParser.LBR, 0); }
		public List<JsonValContext> jsonVal() {
			return getRuleContexts(JsonValContext.class);
		}
		public JsonValContext jsonVal(int i) {
			return getRuleContext(JsonValContext.class,i);
		}
		public TerminalNode RBR() { return getToken(NCIdlParser.RBR, 0); }
		public List<TerminalNode> COMMA() { return getTokens(NCIdlParser.COMMA); }
		public TerminalNode COMMA(int i) {
			return getToken(NCIdlParser.COMMA, i);
		}
		public JsonArrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_jsonArr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterJsonArr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitJsonArr(this);
		}
	}

	public final JsonArrContext jsonArr() throws RecognitionException {
		JsonArrContext _localctx = new JsonArrContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_jsonArr);
		int _la;
		try {
			setState(213);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(200);
				match(LBR);
				setState(201);
				jsonVal();
				setState(206);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(202);
					match(COMMA);
					setState(203);
					jsonVal();
					}
					}
					setState(208);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(209);
				match(RBR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(211);
				match(LBR);
				setState(212);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterTermDecls(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitTermDecls(this);
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
		int _startState = 40;
		enterRecursionRule(_localctx, 40, RULE_termDecls, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(216);
			termDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(222);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermDeclsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_termDecls);
					setState(218);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(219);
					termDecl();
					}
					} 
				}
				setState(224);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterTermDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitTermDecl(this);
		}
	}

	public final TermDeclContext termDecl() throws RecognitionException {
		TermDeclContext _localctx = new TermDeclContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_termDecl);
		try {
			setState(227);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(225);
				term();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(226);
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
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
		public TerminalNode TILDA() { return getToken(NCIdlParser.TILDA, 0); }
		public TermEqContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termEq; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterTermEq(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitTermEq(this);
		}
	}

	public final TermEqContext termEq() throws RecognitionException {
		TermEqContext _localctx = new TermEqContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(229);
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
		public TerminalNode TERM() { return getToken(NCIdlParser.TERM, 0); }
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
		public TerminalNode LBRACE() { return getToken(NCIdlParser.LBRACE, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RBRACE() { return getToken(NCIdlParser.RBRACE, 0); }
		public VarsContext vars() {
			return getRuleContext(VarsContext.class,0);
		}
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231);
			match(TERM);
			setState(233);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAR) {
				{
				setState(232);
				termId();
				}
			}

			setState(235);
			termEq();
			setState(244);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				{
				{
				setState(236);
				match(LBRACE);
				setState(238);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
				case 1:
					{
					setState(237);
					vars(0);
					}
					break;
				}
				setState(240);
				expr(0);
				setState(241);
				match(RBRACE);
				}
				}
				break;
			case DIV:
				{
				setState(243);
				mtdDecl();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(247);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(246);
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
		public TerminalNode POUND() { return getToken(NCIdlParser.POUND, 0); }
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMtdRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMtdRef(this);
		}
	}

	public final MtdRefContext mtdRef() throws RecognitionException {
		MtdRefContext _localctx = new MtdRefContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_mtdRef);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FUN_NAME || _la==ID) {
				{
				setState(249);
				javaFqn(0);
				}
			}

			setState(252);
			match(POUND);
			setState(253);
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
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public JavaFqnContext javaFqn() {
			return getRuleContext(JavaFqnContext.class,0);
		}
		public TerminalNode DOT() { return getToken(NCIdlParser.DOT, 0); }
		public JavaFqnContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_javaFqn; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterJavaFqn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitJavaFqn(this);
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
		int _startState = 50;
		enterRecursionRule(_localctx, 50, RULE_javaFqn, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(256);
			id();
			}
			_ctx.stop = _input.LT(-1);
			setState(263);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new JavaFqnContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_javaFqn);
					setState(258);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(259);
					match(DOT);
					setState(260);
					id();
					}
					} 
				}
				setState(265);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,24,_ctx);
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
		public TerminalNode LPAR() { return getToken(NCIdlParser.LPAR, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIdlParser.RPAR, 0); }
		public TermIdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_termId; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterTermId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitTermId(this);
		}
	}

	public final TermIdContext termId() throws RecognitionException {
		TermIdContext _localctx = new TermIdContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(266);
			match(LPAR);
			setState(267);
			id();
			setState(268);
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
		public TerminalNode LPAR() { return getToken(NCIdlParser.LPAR, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(NCIdlParser.RPAR, 0); }
		public ParExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterParExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitParExpr(this);
		}
	}
	public static class UnaryExprContext extends ExprContext {
		public Token op;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public TerminalNode MINUS() { return getToken(NCIdlParser.MINUS, 0); }
		public TerminalNode NOT() { return getToken(NCIdlParser.NOT, 0); }
		public UnaryExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterUnaryExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitUnaryExpr(this);
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
		public TerminalNode LTEQ() { return getToken(NCIdlParser.LTEQ, 0); }
		public TerminalNode GTEQ() { return getToken(NCIdlParser.GTEQ, 0); }
		public TerminalNode LT() { return getToken(NCIdlParser.LT, 0); }
		public TerminalNode GT() { return getToken(NCIdlParser.GT, 0); }
		public CompExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterCompExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitCompExpr(this);
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
		public TerminalNode PLUS() { return getToken(NCIdlParser.PLUS, 0); }
		public TerminalNode MINUS() { return getToken(NCIdlParser.MINUS, 0); }
		public PlusMinusExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterPlusMinusExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitPlusMinusExpr(this);
		}
	}
	public static class AtomExprContext extends ExprContext {
		public AtomContext atom() {
			return getRuleContext(AtomContext.class,0);
		}
		public AtomExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterAtomExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitAtomExpr(this);
		}
	}
	public static class VarRefContext extends ExprContext {
		public TerminalNode AT() { return getToken(NCIdlParser.AT, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public VarRefContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterVarRef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitVarRef(this);
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
		public TerminalNode MULT() { return getToken(NCIdlParser.MULT, 0); }
		public TerminalNode DIV() { return getToken(NCIdlParser.DIV, 0); }
		public TerminalNode MOD() { return getToken(NCIdlParser.MOD, 0); }
		public MultDivModExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMultDivModExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMultDivModExpr(this);
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
		public TerminalNode AND() { return getToken(NCIdlParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCIdlParser.OR, 0); }
		public AndOrExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterAndOrExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitAndOrExpr(this);
		}
	}
	public static class CallExprContext extends ExprContext {
		public TerminalNode FUN_NAME() { return getToken(NCIdlParser.FUN_NAME, 0); }
		public TerminalNode LPAR() { return getToken(NCIdlParser.LPAR, 0); }
		public TerminalNode RPAR() { return getToken(NCIdlParser.RPAR, 0); }
		public ParamListContext paramList() {
			return getRuleContext(ParamListContext.class,0);
		}
		public CallExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterCallExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitCallExpr(this);
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
		public TerminalNode EQ() { return getToken(NCIdlParser.EQ, 0); }
		public TerminalNode NEQ() { return getToken(NCIdlParser.NEQ, 0); }
		public EqNeqExprContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterEqNeqExpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitEqNeqExpr(this);
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
		int _startState = 54;
		enterRecursionRule(_localctx, 54, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case MINUS:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(271);
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
				setState(272);
				expr(10);
				}
				break;
			case LPAR:
				{
				_localctx = new ParExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(273);
				match(LPAR);
				setState(274);
				expr(0);
				setState(275);
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
				setState(277);
				atom();
				}
				break;
			case FUN_NAME:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(278);
				match(FUN_NAME);
				setState(279);
				match(LPAR);
				setState(281);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << SQSTRING) | (1L << DQSTRING) | (1L << BOOL) | (1L << NULL) | (1L << NOT) | (1L << LPAR) | (1L << MINUS) | (1L << AT) | (1L << INT))) != 0)) {
					{
					setState(280);
					paramList(0);
					}
				}

				setState(283);
				match(RPAR);
				}
				break;
			case AT:
				{
				_localctx = new VarRefContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(284);
				match(AT);
				setState(285);
				id();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(305);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,28,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(303);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,27,_ctx) ) {
					case 1:
						{
						_localctx = new MultDivModExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(288);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(289);
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
						setState(290);
						expr(9);
						}
						break;
					case 2:
						{
						_localctx = new PlusMinusExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(291);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(292);
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
						setState(293);
						expr(8);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(294);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(295);
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
						setState(296);
						expr(7);
						}
						break;
					case 4:
						{
						_localctx = new EqNeqExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(297);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(298);
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
						setState(299);
						expr(6);
						}
						break;
					case 5:
						{
						_localctx = new AndOrExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(300);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(301);
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
						setState(302);
						expr(5);
						}
						break;
					}
					} 
				}
				setState(307);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterVars(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitVars(this);
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
		int _startState = 56;
		enterRecursionRule(_localctx, 56, RULE_vars, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(309);
			varDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(315);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new VarsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_vars);
					setState(311);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(312);
					varDecl();
					}
					} 
				}
				setState(317);
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

	public static class VarDeclContext extends ParserRuleContext {
		public TerminalNode AT() { return getToken(NCIdlParser.AT, 0); }
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode ASSIGN() { return getToken(NCIdlParser.ASSIGN, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterVarDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitVarDecl(this);
		}
	}

	public final VarDeclContext varDecl() throws RecognitionException {
		VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_varDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(318);
			match(AT);
			setState(319);
			id();
			setState(320);
			match(ASSIGN);
			setState(321);
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
		public TerminalNode COMMA() { return getToken(NCIdlParser.COMMA, 0); }
		public ParamListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_paramList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterParamList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitParamList(this);
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
		int _startState = 60;
		enterRecursionRule(_localctx, 60, RULE_paramList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(324);
			expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(331);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ParamListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_paramList);
					setState(326);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(327);
					match(COMMA);
					setState(328);
					expr(0);
					}
					} 
				}
				setState(333);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
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
		public TerminalNode NULL() { return getToken(NCIdlParser.NULL, 0); }
		public TerminalNode INT() { return getToken(NCIdlParser.INT, 0); }
		public TerminalNode REAL() { return getToken(NCIdlParser.REAL, 0); }
		public TerminalNode EXP() { return getToken(NCIdlParser.EXP, 0); }
		public TerminalNode BOOL() { return getToken(NCIdlParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public AtomContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_atom; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterAtom(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitAtom(this);
		}
	}

	public final AtomContext atom() throws RecognitionException {
		AtomContext _localctx = new AtomContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_atom);
		try {
			setState(344);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(334);
				match(NULL);
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(335);
				match(INT);
				setState(337);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
				case 1:
					{
					setState(336);
					match(REAL);
					}
					break;
				}
				setState(340);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(339);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(342);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(343);
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
		public TerminalNode SQSTRING() { return getToken(NCIdlParser.SQSTRING, 0); }
		public TerminalNode DQSTRING() { return getToken(NCIdlParser.DQSTRING, 0); }
		public QstringContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_qstring; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterQstring(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitQstring(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 64, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(346);
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
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMinMax(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMinMax(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_minMax);
		try {
			setState(350);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case MULT:
				enterOuterAlt(_localctx, 1);
				{
				setState(348);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(349);
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
		public TerminalNode PLUS() { return getToken(NCIdlParser.PLUS, 0); }
		public TerminalNode QUESTION() { return getToken(NCIdlParser.QUESTION, 0); }
		public TerminalNode MULT() { return getToken(NCIdlParser.MULT, 0); }
		public MinMaxShortcutContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxShortcut; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMinMaxShortcut(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMinMaxShortcut(this);
		}
	}

	public final MinMaxShortcutContext minMaxShortcut() throws RecognitionException {
		MinMaxShortcutContext _localctx = new MinMaxShortcutContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(352);
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
		public TerminalNode LBR() { return getToken(NCIdlParser.LBR, 0); }
		public List<TerminalNode> INT() { return getTokens(NCIdlParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(NCIdlParser.INT, i);
		}
		public TerminalNode COMMA() { return getToken(NCIdlParser.COMMA, 0); }
		public TerminalNode RBR() { return getToken(NCIdlParser.RBR, 0); }
		public MinMaxRangeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_minMaxRange; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterMinMaxRange(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitMinMaxRange(this);
		}
	}

	public final MinMaxRangeContext minMaxRange() throws RecognitionException {
		MinMaxRangeContext _localctx = new MinMaxRangeContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(354);
			match(LBR);
			setState(355);
			match(INT);
			setState(356);
			match(COMMA);
			setState(357);
			match(INT);
			setState(358);
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
		public TerminalNode ID() { return getToken(NCIdlParser.ID, 0); }
		public TerminalNode FUN_NAME() { return getToken(NCIdlParser.FUN_NAME, 0); }
		public IdContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_id; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterId(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitId(this);
		}
	}

	public final IdContext id() throws RecognitionException {
		IdContext _localctx = new IdContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_id);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(360);
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
		case 3:
			return idlDecls_sempred((IdlDeclsContext)_localctx, predIndex);
		case 20:
			return termDecls_sempred((TermDeclsContext)_localctx, predIndex);
		case 25:
			return javaFqn_sempred((JavaFqnContext)_localctx, predIndex);
		case 27:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 28:
			return vars_sempred((VarsContext)_localctx, predIndex);
		case 30:
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
			return precpred(_ctx, 8);
		case 4:
			return precpred(_ctx, 7);
		case 5:
			return precpred(_ctx, 6);
		case 6:
			return precpred(_ctx, 5);
		case 7:
			return precpred(_ctx, 4);
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\66\u016d\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\3\2\3\2\3\3\5\3Q\n\3\3\3\3\3\5"+
		"\3U\n\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\7\5d\n\5\f"+
		"\5\16\5g\13\5\3\6\3\6\3\6\5\6l\n\6\3\7\3\7\3\7\3\7\3\7\3\b\3\b\3\b\3\t"+
		"\3\t\3\t\3\t\3\n\3\n\3\n\3\n\5\n~\n\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\5"+
		"\f\u0087\n\f\3\f\5\f\u008a\n\f\3\f\5\f\u008d\n\f\3\f\3\f\3\r\3\r\3\r\3"+
		"\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\5\20\u00a1"+
		"\n\20\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\7\22\u00ab\n\22\f\22\16"+
		"\22\u00ae\13\22\3\22\3\22\3\22\3\22\5\22\u00b4\n\22\3\23\3\23\3\23\3\23"+
		"\3\24\3\24\5\24\u00bc\n\24\3\24\3\24\5\24\u00c0\n\24\3\24\5\24\u00c3\n"+
		"\24\3\24\3\24\3\24\3\24\5\24\u00c9\n\24\3\25\3\25\3\25\3\25\7\25\u00cf"+
		"\n\25\f\25\16\25\u00d2\13\25\3\25\3\25\3\25\3\25\5\25\u00d8\n\25\3\26"+
		"\3\26\3\26\3\26\3\26\7\26\u00df\n\26\f\26\16\26\u00e2\13\26\3\27\3\27"+
		"\5\27\u00e6\n\27\3\30\3\30\3\31\3\31\5\31\u00ec\n\31\3\31\3\31\3\31\5"+
		"\31\u00f1\n\31\3\31\3\31\3\31\3\31\5\31\u00f7\n\31\3\31\5\31\u00fa\n\31"+
		"\3\32\5\32\u00fd\n\32\3\32\3\32\3\32\3\33\3\33\3\33\3\33\3\33\3\33\7\33"+
		"\u0108\n\33\f\33\16\33\u010b\13\33\3\34\3\34\3\34\3\34\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\5\35\u011c\n\35\3\35\3\35\3\35"+
		"\5\35\u0121\n\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35\3\35"+
		"\3\35\3\35\3\35\3\35\7\35\u0132\n\35\f\35\16\35\u0135\13\35\3\36\3\36"+
		"\3\36\3\36\3\36\7\36\u013c\n\36\f\36\16\36\u013f\13\36\3\37\3\37\3\37"+
		"\3\37\3\37\3 \3 \3 \3 \3 \3 \7 \u014c\n \f \16 \u014f\13 \3!\3!\3!\5!"+
		"\u0154\n!\3!\5!\u0157\n!\3!\3!\5!\u015b\n!\3\"\3\"\3#\3#\5#\u0161\n#\3"+
		"$\3$\3%\3%\3%\3%\3%\3%\3&\3&\3&\2\b\b*\648:>\'\2\4\6\b\n\f\16\20\22\24"+
		"\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJ\2\f\4\2\37\37((\4\2\30"+
		"\30%%\3\2+-\4\2%%))\3\2\21\24\3\2\17\20\3\2\25\26\3\2\13\f\3\2)+\4\2\3"+
		"\3\63\63\2\u0177\2L\3\2\2\2\4P\3\2\2\2\6Z\3\2\2\2\b^\3\2\2\2\nk\3\2\2"+
		"\2\fm\3\2\2\2\16r\3\2\2\2\20u\3\2\2\2\22y\3\2\2\2\24\u0081\3\2\2\2\26"+
		"\u0084\3\2\2\2\30\u0090\3\2\2\2\32\u0094\3\2\2\2\34\u0098\3\2\2\2\36\u009c"+
		"\3\2\2\2 \u00a2\3\2\2\2\"\u00b3\3\2\2\2$\u00b5\3\2\2\2&\u00c8\3\2\2\2"+
		"(\u00d7\3\2\2\2*\u00d9\3\2\2\2,\u00e5\3\2\2\2.\u00e7\3\2\2\2\60\u00e9"+
		"\3\2\2\2\62\u00fc\3\2\2\2\64\u0101\3\2\2\2\66\u010c\3\2\2\28\u0120\3\2"+
		"\2\2:\u0136\3\2\2\2<\u0140\3\2\2\2>\u0145\3\2\2\2@\u015a\3\2\2\2B\u015c"+
		"\3\2\2\2D\u0160\3\2\2\2F\u0162\3\2\2\2H\u0164\3\2\2\2J\u016a\3\2\2\2L"+
		"M\5\b\5\2MN\7\2\2\3N\3\3\2\2\2OQ\5\6\4\2PO\3\2\2\2PQ\3\2\2\2QR\3\2\2\2"+
		"RT\7\33\2\2SU\5:\36\2TS\3\2\2\2TU\3\2\2\2UV\3\2\2\2VW\58\35\2WX\7\34\2"+
		"\2XY\7\2\2\3Y\5\3\2\2\2Z[\7 \2\2[\\\5J&\2\\]\7!\2\2]\7\3\2\2\2^_\b\5\1"+
		"\2_`\5\n\6\2`e\3\2\2\2ab\f\3\2\2bd\5\n\6\2ca\3\2\2\2dg\3\2\2\2ec\3\2\2"+
		"\2ef\3\2\2\2f\t\3\2\2\2ge\3\2\2\2hl\5\26\f\2il\5\16\b\2jl\5\f\7\2kh\3"+
		"\2\2\2ki\3\2\2\2kj\3\2\2\2l\13\3\2\2\2mn\7\4\2\2no\7\31\2\2op\5B\"\2p"+
		"q\7\32\2\2q\r\3\2\2\2rs\5\20\t\2st\5*\26\2t\17\3\2\2\2uv\7\n\2\2vw\7("+
		"\2\2wx\5J&\2x\21\3\2\2\2yz\7\n\2\2z{\7\31\2\2{}\5J&\2|~\5\24\13\2}|\3"+
		"\2\2\2}~\3\2\2\2~\177\3\2\2\2\177\u0080\7\32\2\2\u0080\23\3\2\2\2\u0081"+
		"\u0082\7#\2\2\u0082\u0083\5\"\22\2\u0083\25\3\2\2\2\u0084\u0086\5\30\r"+
		"\2\u0085\u0087\5\32\16\2\u0086\u0085\3\2\2\2\u0086\u0087\3\2\2\2\u0087"+
		"\u0089\3\2\2\2\u0088\u008a\5\36\20\2\u0089\u0088\3\2\2\2\u0089\u008a\3"+
		"\2\2\2\u008a\u008c\3\2\2\2\u008b\u008d\5 \21\2\u008c\u008b\3\2\2\2\u008c"+
		"\u008d\3\2\2\2\u008d\u008e\3\2\2\2\u008e\u008f\5*\26\2\u008f\27\3\2\2"+
		"\2\u0090\u0091\7\5\2\2\u0091\u0092\7(\2\2\u0092\u0093\5J&\2\u0093\31\3"+
		"\2\2\2\u0094\u0095\7\6\2\2\u0095\u0096\7(\2\2\u0096\u0097\7\r\2\2\u0097"+
		"\33\3\2\2\2\u0098\u0099\7,\2\2\u0099\u009a\5\62\32\2\u009a\u009b\7,\2"+
		"\2\u009b\35\3\2\2\2\u009c\u009d\7\7\2\2\u009d\u00a0\7(\2\2\u009e\u00a1"+
		"\5B\"\2\u009f\u00a1\5\34\17\2\u00a0\u009e\3\2\2\2\u00a0\u009f\3\2\2\2"+
		"\u00a1\37\3\2\2\2\u00a2\u00a3\7\b\2\2\u00a3\u00a4\7(\2\2\u00a4\u00a5\5"+
		"\"\22\2\u00a5!\3\2\2\2\u00a6\u00a7\7\33\2\2\u00a7\u00ac\5$\23\2\u00a8"+
		"\u00a9\7#\2\2\u00a9\u00ab\5$\23\2\u00aa\u00a8\3\2\2\2\u00ab\u00ae\3\2"+
		"\2\2\u00ac\u00aa\3\2\2\2\u00ac\u00ad\3\2\2\2\u00ad\u00af\3\2\2\2\u00ae"+
		"\u00ac\3\2\2\2\u00af\u00b0\7\34\2\2\u00b0\u00b4\3\2\2\2\u00b1\u00b2\7"+
		"\33\2\2\u00b2\u00b4\7\34\2\2\u00b3\u00a6\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b4"+
		"#\3\2\2\2\u00b5\u00b6\5B\"\2\u00b6\u00b7\7$\2\2\u00b7\u00b8\5&\24\2\u00b8"+
		"%\3\2\2\2\u00b9\u00c9\5B\"\2\u00ba\u00bc\7%\2\2\u00bb\u00ba\3\2\2\2\u00bb"+
		"\u00bc\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00bf\7\60\2\2\u00be\u00c0\7"+
		"\61\2\2\u00bf\u00be\3\2\2\2\u00bf\u00c0\3\2\2\2\u00c0\u00c2\3\2\2\2\u00c1"+
		"\u00c3\7\62\2\2\u00c2\u00c1\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3\u00c9\3"+
		"\2\2\2\u00c4\u00c9\5\"\22\2\u00c5\u00c9\5(\25\2\u00c6\u00c9\7\r\2\2\u00c7"+
		"\u00c9\7\16\2\2\u00c8\u00b9\3\2\2\2\u00c8\u00bb\3\2\2\2\u00c8\u00c4\3"+
		"\2\2\2\u00c8\u00c5\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8\u00c7\3\2\2\2\u00c9"+
		"\'\3\2\2\2\u00ca\u00cb\7 \2\2\u00cb\u00d0\5&\24\2\u00cc\u00cd\7#\2\2\u00cd"+
		"\u00cf\5&\24\2\u00ce\u00cc\3\2\2\2\u00cf\u00d2\3\2\2\2\u00d0\u00ce\3\2"+
		"\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d3\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d3"+
		"\u00d4\7!\2\2\u00d4\u00d8\3\2\2\2\u00d5\u00d6\7 \2\2\u00d6\u00d8\7!\2"+
		"\2\u00d7\u00ca\3\2\2\2\u00d7\u00d5\3\2\2\2\u00d8)\3\2\2\2\u00d9\u00da"+
		"\b\26\1\2\u00da\u00db\5,\27\2\u00db\u00e0\3\2\2\2\u00dc\u00dd\f\3\2\2"+
		"\u00dd\u00df\5,\27\2\u00de\u00dc\3\2\2\2\u00df\u00e2\3\2\2\2\u00e0\u00de"+
		"\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1+\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e3"+
		"\u00e6\5\60\31\2\u00e4\u00e6\5\22\n\2\u00e5\u00e3\3\2\2\2\u00e5\u00e4"+
		"\3\2\2\2\u00e6-\3\2\2\2\u00e7\u00e8\t\2\2\2\u00e8/\3\2\2\2\u00e9\u00eb"+
		"\7\t\2\2\u00ea\u00ec\5\66\34\2\u00eb\u00ea\3\2\2\2\u00eb\u00ec\3\2\2\2"+
		"\u00ec\u00ed\3\2\2\2\u00ed\u00f6\5.\30\2\u00ee\u00f0\7\33\2\2\u00ef\u00f1"+
		"\5:\36\2\u00f0\u00ef\3\2\2\2\u00f0\u00f1\3\2\2\2\u00f1\u00f2\3\2\2\2\u00f2"+
		"\u00f3\58\35\2\u00f3\u00f4\7\34\2\2\u00f4\u00f7\3\2\2\2\u00f5\u00f7\5"+
		"\34\17\2\u00f6\u00ee\3\2\2\2\u00f6\u00f5\3\2\2\2\u00f7\u00f9\3\2\2\2\u00f8"+
		"\u00fa\5D#\2\u00f9\u00f8\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa\61\3\2\2\2"+
		"\u00fb\u00fd\5\64\33\2\u00fc\u00fb\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fe"+
		"\3\2\2\2\u00fe\u00ff\7\"\2\2\u00ff\u0100\5J&\2\u0100\63\3\2\2\2\u0101"+
		"\u0102\b\33\1\2\u0102\u0103\5J&\2\u0103\u0109\3\2\2\2\u0104\u0105\f\3"+
		"\2\2\u0105\u0106\7&\2\2\u0106\u0108\5J&\2\u0107\u0104\3\2\2\2\u0108\u010b"+
		"\3\2\2\2\u0109\u0107\3\2\2\2\u0109\u010a\3\2\2\2\u010a\65\3\2\2\2\u010b"+
		"\u0109\3\2\2\2\u010c\u010d\7\31\2\2\u010d\u010e\5J&\2\u010e\u010f\7\32"+
		"\2\2\u010f\67\3\2\2\2\u0110\u0111\b\35\1\2\u0111\u0112\t\3\2\2\u0112\u0121"+
		"\58\35\f\u0113\u0114\7\31\2\2\u0114\u0115\58\35\2\u0115\u0116\7\32\2\2"+
		"\u0116\u0121\3\2\2\2\u0117\u0121\5@!\2\u0118\u0119\7\3\2\2\u0119\u011b"+
		"\7\31\2\2\u011a\u011c\5> \2\u011b\u011a\3\2\2\2\u011b\u011c\3\2\2\2\u011c"+
		"\u011d\3\2\2\2\u011d\u0121\7\32\2\2\u011e\u011f\7.\2\2\u011f\u0121\5J"+
		"&\2\u0120\u0110\3\2\2\2\u0120\u0113\3\2\2\2\u0120\u0117\3\2\2\2\u0120"+
		"\u0118\3\2\2\2\u0120\u011e\3\2\2\2\u0121\u0133\3\2\2\2\u0122\u0123\f\n"+
		"\2\2\u0123\u0124\t\4\2\2\u0124\u0132\58\35\13\u0125\u0126\f\t\2\2\u0126"+
		"\u0127\t\5\2\2\u0127\u0132\58\35\n\u0128\u0129\f\b\2\2\u0129\u012a\t\6"+
		"\2\2\u012a\u0132\58\35\t\u012b\u012c\f\7\2\2\u012c\u012d\t\7\2\2\u012d"+
		"\u0132\58\35\b\u012e\u012f\f\6\2\2\u012f\u0130\t\b\2\2\u0130\u0132\58"+
		"\35\7\u0131\u0122\3\2\2\2\u0131\u0125\3\2\2\2\u0131\u0128\3\2\2\2\u0131"+
		"\u012b\3\2\2\2\u0131\u012e\3\2\2\2\u0132\u0135\3\2\2\2\u0133\u0131\3\2"+
		"\2\2\u0133\u0134\3\2\2\2\u01349\3\2\2\2\u0135\u0133\3\2\2\2\u0136\u0137"+
		"\b\36\1\2\u0137\u0138\5<\37\2\u0138\u013d\3\2\2\2\u0139\u013a\f\3\2\2"+
		"\u013a\u013c\5<\37\2\u013b\u0139\3\2\2\2\u013c\u013f\3\2\2\2\u013d\u013b"+
		"\3\2\2\2\u013d\u013e\3\2\2\2\u013e;\3\2\2\2\u013f\u013d\3\2\2\2\u0140"+
		"\u0141\7.\2\2\u0141\u0142\5J&\2\u0142\u0143\7(\2\2\u0143\u0144\58\35\2"+
		"\u0144=\3\2\2\2\u0145\u0146\b \1\2\u0146\u0147\58\35\2\u0147\u014d\3\2"+
		"\2\2\u0148\u0149\f\3\2\2\u0149\u014a\7#\2\2\u014a\u014c\58\35\2\u014b"+
		"\u0148\3\2\2\2\u014c\u014f\3\2\2\2\u014d\u014b\3\2\2\2\u014d\u014e\3\2"+
		"\2\2\u014e?\3\2\2\2\u014f\u014d\3\2\2\2\u0150\u015b\7\16\2\2\u0151\u0153"+
		"\7\60\2\2\u0152\u0154\7\61\2\2\u0153\u0152\3\2\2\2\u0153\u0154\3\2\2\2"+
		"\u0154\u0156\3\2\2\2\u0155\u0157\7\62\2\2\u0156\u0155\3\2\2\2\u0156\u0157"+
		"\3\2\2\2\u0157\u015b\3\2\2\2\u0158\u015b\7\r\2\2\u0159\u015b\5B\"\2\u015a"+
		"\u0150\3\2\2\2\u015a\u0151\3\2\2\2\u015a\u0158\3\2\2\2\u015a\u0159\3\2"+
		"\2\2\u015bA\3\2\2\2\u015c\u015d\t\t\2\2\u015dC\3\2\2\2\u015e\u0161\5F"+
		"$\2\u015f\u0161\5H%\2\u0160\u015e\3\2\2\2\u0160\u015f\3\2\2\2\u0161E\3"+
		"\2\2\2\u0162\u0163\t\n\2\2\u0163G\3\2\2\2\u0164\u0165\7 \2\2\u0165\u0166"+
		"\7\60\2\2\u0166\u0167\7#\2\2\u0167\u0168\7\60\2\2\u0168\u0169\7!\2\2\u0169"+
		"I\3\2\2\2\u016a\u016b\t\13\2\2\u016bK\3\2\2\2%PTek}\u0086\u0089\u008c"+
		"\u00a0\u00ac\u00b3\u00bb\u00bf\u00c2\u00c8\u00d0\u00d7\u00e0\u00e5\u00eb"+
		"\u00f0\u00f6\u00f9\u00fc\u0109\u011b\u0120\u0131\u0133\u013d\u014d\u0153"+
		"\u0156\u015a\u0160";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}