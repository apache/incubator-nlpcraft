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
		RULE_javaClass = 26, RULE_termId = 27, RULE_expr = 28, RULE_vars = 29, 
		RULE_varDecl = 30, RULE_paramList = 31, RULE_atom = 32, RULE_qstring = 33, 
		RULE_minMax = 34, RULE_minMaxShortcut = 35, RULE_minMaxRange = 36, RULE_id = 37;
	private static String[] makeRuleNames() {
		return new String[] {
			"idl", "synonym", "alias", "idlDecls", "idlDecl", "imp", "frag", "fragId", 
			"fragRef", "fragMeta", "intent", "intentId", "orderedDecl", "mtdDecl", 
			"flowDecl", "metaDecl", "jsonObj", "jsonPair", "jsonVal", "jsonArr", 
			"termDecls", "termDecl", "termEq", "term", "mtdRef", "javaFqn", "javaClass", 
			"termId", "expr", "vars", "varDecl", "paramList", "atom", "qstring", 
			"minMax", "minMaxShortcut", "minMaxRange", "id"
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
			setState(76);
			idlDecls(0);
			setState(77);
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
			setState(80);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LBR) {
				{
				setState(79);
				alias();
				}
			}

			setState(82);
			match(LBRACE);
			setState(84);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				{
				setState(83);
				vars(0);
				}
				break;
			}
			setState(86);
			expr(0);
			setState(87);
			match(RBRACE);
			setState(88);
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
			setState(90);
			match(LBR);
			setState(91);
			id();
			setState(92);
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
			setState(95);
			idlDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(101);
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
					setState(97);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(98);
					idlDecl();
					}
					} 
				}
				setState(103);
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
			setState(107);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case INTENT:
				enterOuterAlt(_localctx, 1);
				{
				setState(104);
				intent();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(105);
				frag();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 3);
				{
				setState(106);
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
			setState(109);
			match(IMPORT);
			setState(110);
			match(LPAR);
			setState(111);
			qstring();
			setState(112);
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
			setState(114);
			fragId();
			setState(115);
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
			setState(117);
			match(FRAG);
			setState(118);
			match(ASSIGN);
			setState(119);
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
			setState(121);
			match(FRAG);
			setState(122);
			match(LPAR);
			setState(123);
			id();
			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==COMMA) {
				{
				setState(124);
				fragMeta();
				}
			}

			setState(127);
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
			setState(129);
			match(COMMA);
			setState(130);
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
			setState(132);
			intentId();
			setState(134);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ORDERED) {
				{
				setState(133);
				orderedDecl();
				}
			}

			setState(137);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==FLOW) {
				{
				setState(136);
				flowDecl();
				}
			}

			setState(140);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==META) {
				{
				setState(139);
				metaDecl();
				}
			}

			setState(142);
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
			setState(144);
			match(INTENT);
			setState(145);
			match(ASSIGN);
			setState(146);
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
			setState(148);
			match(ORDERED);
			setState(149);
			match(ASSIGN);
			setState(150);
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
			setState(152);
			match(DIV);
			setState(153);
			mtdRef();
			setState(154);
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
			setState(156);
			match(FLOW);
			setState(157);
			match(ASSIGN);
			setState(160);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				{
				setState(158);
				qstring();
				}
				break;
			case DIV:
				{
				setState(159);
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
			setState(162);
			match(META);
			setState(163);
			match(ASSIGN);
			setState(164);
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
			setState(179);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(166);
				match(LBRACE);
				setState(167);
				jsonPair();
				setState(172);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(168);
					match(COMMA);
					setState(169);
					jsonPair();
					}
					}
					setState(174);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(175);
				match(RBRACE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(177);
				match(LBRACE);
				setState(178);
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
			setState(181);
			qstring();
			setState(182);
			match(COLON);
			setState(183);
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
			setState(200);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 1);
				{
				setState(185);
				qstring();
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(187);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(186);
					match(MINUS);
					}
				}

				setState(189);
				match(INT);
				setState(191);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==REAL) {
					{
					setState(190);
					match(REAL);
					}
				}

				setState(194);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==EXP) {
					{
					setState(193);
					match(EXP);
					}
				}

				}
				break;
			case LBRACE:
				enterOuterAlt(_localctx, 3);
				{
				setState(196);
				jsonObj();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 4);
				{
				setState(197);
				jsonArr();
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 5);
				{
				setState(198);
				match(BOOL);
				}
				break;
			case NULL:
				enterOuterAlt(_localctx, 6);
				{
				setState(199);
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
			setState(215);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(202);
				match(LBR);
				setState(203);
				jsonVal();
				setState(208);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==COMMA) {
					{
					{
					setState(204);
					match(COMMA);
					setState(205);
					jsonVal();
					}
					}
					setState(210);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(211);
				match(RBR);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(213);
				match(LBR);
				setState(214);
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
			setState(218);
			termDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(224);
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
					setState(220);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(221);
					termDecl();
					}
					} 
				}
				setState(226);
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
			setState(229);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TERM:
				enterOuterAlt(_localctx, 1);
				{
				setState(227);
				term();
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 2);
				{
				setState(228);
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
			setState(231);
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
			setState(233);
			match(TERM);
			setState(235);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAR) {
				{
				setState(234);
				termId();
				}
			}

			setState(237);
			termEq();
			setState(246);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case LBRACE:
				{
				{
				setState(238);
				match(LBRACE);
				setState(240);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
				case 1:
					{
					setState(239);
					vars(0);
					}
					break;
				}
				setState(242);
				expr(0);
				setState(243);
				match(RBRACE);
				}
				}
				break;
			case DIV:
				{
				setState(245);
				mtdDecl();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(249);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				{
				setState(248);
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
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << IMPORT) | (1L << INTENT) | (1L << ORDERED) | (1L << FLOW) | (1L << META) | (1L << TERM) | (1L << FRAG) | (1L << ID))) != 0)) {
				{
				setState(251);
				javaFqn(0);
				}
			}

			setState(254);
			match(POUND);
			setState(255);
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
			setState(258);
			javaClass();
			}
			_ctx.stop = _input.LT(-1);
			setState(265);
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
					setState(260);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(261);
					match(DOT);
					setState(262);
					javaClass();
					}
					} 
				}
				setState(267);
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

	public static class JavaClassContext extends ParserRuleContext {
		public IdContext id() {
			return getRuleContext(IdContext.class,0);
		}
		public TerminalNode IMPORT() { return getToken(NCIdlParser.IMPORT, 0); }
		public TerminalNode INTENT() { return getToken(NCIdlParser.INTENT, 0); }
		public TerminalNode ORDERED() { return getToken(NCIdlParser.ORDERED, 0); }
		public TerminalNode FLOW() { return getToken(NCIdlParser.FLOW, 0); }
		public TerminalNode META() { return getToken(NCIdlParser.META, 0); }
		public TerminalNode TERM() { return getToken(NCIdlParser.TERM, 0); }
		public TerminalNode FRAG() { return getToken(NCIdlParser.FRAG, 0); }
		public JavaClassContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_javaClass; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).enterJavaClass(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIdlListener ) ((NCIdlListener)listener).exitJavaClass(this);
		}
	}

	public final JavaClassContext javaClass() throws RecognitionException {
		JavaClassContext _localctx = new JavaClassContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_javaClass);
		try {
			setState(276);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case FUN_NAME:
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(268);
				id();
				}
				break;
			case IMPORT:
				enterOuterAlt(_localctx, 2);
				{
				setState(269);
				match(IMPORT);
				}
				break;
			case INTENT:
				enterOuterAlt(_localctx, 3);
				{
				setState(270);
				match(INTENT);
				}
				break;
			case ORDERED:
				enterOuterAlt(_localctx, 4);
				{
				setState(271);
				match(ORDERED);
				}
				break;
			case FLOW:
				enterOuterAlt(_localctx, 5);
				{
				setState(272);
				match(FLOW);
				}
				break;
			case META:
				enterOuterAlt(_localctx, 6);
				{
				setState(273);
				match(META);
				}
				break;
			case TERM:
				enterOuterAlt(_localctx, 7);
				{
				setState(274);
				match(TERM);
				}
				break;
			case FRAG:
				enterOuterAlt(_localctx, 8);
				{
				setState(275);
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
		enterRule(_localctx, 54, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
			match(LPAR);
			setState(279);
			id();
			setState(280);
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
		int _startState = 56;
		enterRecursionRule(_localctx, 56, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(298);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
			case MINUS:
				{
				_localctx = new UnaryExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(283);
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
				setState(284);
				expr(10);
				}
				break;
			case LPAR:
				{
				_localctx = new ParExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(285);
				match(LPAR);
				setState(286);
				expr(0);
				setState(287);
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
				setState(289);
				atom();
				}
				break;
			case FUN_NAME:
				{
				_localctx = new CallExprContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(290);
				match(FUN_NAME);
				setState(291);
				match(LPAR);
				setState(293);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << FUN_NAME) | (1L << SQSTRING) | (1L << DQSTRING) | (1L << BOOL) | (1L << NULL) | (1L << NOT) | (1L << LPAR) | (1L << MINUS) | (1L << AT) | (1L << INT))) != 0)) {
					{
					setState(292);
					paramList(0);
					}
				}

				setState(295);
				match(RPAR);
				}
				break;
			case AT:
				{
				_localctx = new VarRefContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(296);
				match(AT);
				setState(297);
				id();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(317);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(315);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
					case 1:
						{
						_localctx = new MultDivModExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(300);
						if (!(precpred(_ctx, 8))) throw new FailedPredicateException(this, "precpred(_ctx, 8)");
						setState(301);
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
						setState(302);
						expr(9);
						}
						break;
					case 2:
						{
						_localctx = new PlusMinusExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(303);
						if (!(precpred(_ctx, 7))) throw new FailedPredicateException(this, "precpred(_ctx, 7)");
						setState(304);
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
						setState(305);
						expr(8);
						}
						break;
					case 3:
						{
						_localctx = new CompExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(306);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(307);
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
						setState(308);
						expr(7);
						}
						break;
					case 4:
						{
						_localctx = new EqNeqExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(309);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(310);
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
						setState(311);
						expr(6);
						}
						break;
					case 5:
						{
						_localctx = new AndOrExprContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(312);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(313);
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
						setState(314);
						expr(5);
						}
						break;
					}
					} 
				}
				setState(319);
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
		int _startState = 58;
		enterRecursionRule(_localctx, 58, RULE_vars, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(321);
			varDecl();
			}
			_ctx.stop = _input.LT(-1);
			setState(327);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,30,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new VarsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_vars);
					setState(323);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(324);
					varDecl();
					}
					} 
				}
				setState(329);
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
		enterRule(_localctx, 60, RULE_varDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(330);
			match(AT);
			setState(331);
			id();
			setState(332);
			match(ASSIGN);
			setState(333);
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
		int _startState = 62;
		enterRecursionRule(_localctx, 62, RULE_paramList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(336);
			expr(0);
			}
			_ctx.stop = _input.LT(-1);
			setState(343);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ParamListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_paramList);
					setState(338);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(339);
					match(COMMA);
					setState(340);
					expr(0);
					}
					} 
				}
				setState(345);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,31,_ctx);
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
		enterRule(_localctx, 64, RULE_atom);
		try {
			setState(356);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NULL:
				enterOuterAlt(_localctx, 1);
				{
				setState(346);
				match(NULL);
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(347);
				match(INT);
				setState(349);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,32,_ctx) ) {
				case 1:
					{
					setState(348);
					match(REAL);
					}
					break;
				}
				setState(352);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
				case 1:
					{
					setState(351);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(354);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(355);
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
		enterRule(_localctx, 66, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(358);
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
		enterRule(_localctx, 68, RULE_minMax);
		try {
			setState(362);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case MULT:
				enterOuterAlt(_localctx, 1);
				{
				setState(360);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(361);
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
		enterRule(_localctx, 70, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(364);
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
		enterRule(_localctx, 72, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(366);
			match(LBR);
			setState(367);
			match(INT);
			setState(368);
			match(COMMA);
			setState(369);
			match(INT);
			setState(370);
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
		enterRule(_localctx, 74, RULE_id);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(372);
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
		case 28:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 29:
			return vars_sempred((VarsContext)_localctx, predIndex);
		case 31:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\66\u0179\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\3\2\3\2\3\2\3\3\5\3S\n\3\3"+
		"\3\3\3\5\3W\n\3\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\7"+
		"\5f\n\5\f\5\16\5i\13\5\3\6\3\6\3\6\5\6n\n\6\3\7\3\7\3\7\3\7\3\7\3\b\3"+
		"\b\3\b\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\5\n\u0080\n\n\3\n\3\n\3\13\3\13"+
		"\3\13\3\f\3\f\5\f\u0089\n\f\3\f\5\f\u008c\n\f\3\f\5\f\u008f\n\f\3\f\3"+
		"\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\20\3\20\3"+
		"\20\3\20\5\20\u00a3\n\20\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\7\22"+
		"\u00ad\n\22\f\22\16\22\u00b0\13\22\3\22\3\22\3\22\3\22\5\22\u00b6\n\22"+
		"\3\23\3\23\3\23\3\23\3\24\3\24\5\24\u00be\n\24\3\24\3\24\5\24\u00c2\n"+
		"\24\3\24\5\24\u00c5\n\24\3\24\3\24\3\24\3\24\5\24\u00cb\n\24\3\25\3\25"+
		"\3\25\3\25\7\25\u00d1\n\25\f\25\16\25\u00d4\13\25\3\25\3\25\3\25\3\25"+
		"\5\25\u00da\n\25\3\26\3\26\3\26\3\26\3\26\7\26\u00e1\n\26\f\26\16\26\u00e4"+
		"\13\26\3\27\3\27\5\27\u00e8\n\27\3\30\3\30\3\31\3\31\5\31\u00ee\n\31\3"+
		"\31\3\31\3\31\5\31\u00f3\n\31\3\31\3\31\3\31\3\31\5\31\u00f9\n\31\3\31"+
		"\5\31\u00fc\n\31\3\32\5\32\u00ff\n\32\3\32\3\32\3\32\3\33\3\33\3\33\3"+
		"\33\3\33\3\33\7\33\u010a\n\33\f\33\16\33\u010d\13\33\3\34\3\34\3\34\3"+
		"\34\3\34\3\34\3\34\3\34\5\34\u0117\n\34\3\35\3\35\3\35\3\35\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\5\36\u0128\n\36\3\36\3\36"+
		"\3\36\5\36\u012d\n\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36\3\36"+
		"\3\36\3\36\3\36\3\36\3\36\7\36\u013e\n\36\f\36\16\36\u0141\13\36\3\37"+
		"\3\37\3\37\3\37\3\37\7\37\u0148\n\37\f\37\16\37\u014b\13\37\3 \3 \3 \3"+
		" \3 \3!\3!\3!\3!\3!\3!\7!\u0158\n!\f!\16!\u015b\13!\3\"\3\"\3\"\5\"\u0160"+
		"\n\"\3\"\5\"\u0163\n\"\3\"\3\"\5\"\u0167\n\"\3#\3#\3$\3$\5$\u016d\n$\3"+
		"%\3%\3&\3&\3&\3&\3&\3&\3\'\3\'\3\'\2\b\b*\64:<@(\2\4\6\b\n\f\16\20\22"+
		"\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJL\2\f\4\2\37\37((\4\2"+
		"\30\30%%\3\2+-\4\2%%))\3\2\21\24\3\2\17\20\3\2\25\26\3\2\13\f\3\2)+\4"+
		"\2\3\3\63\63\2\u0189\2N\3\2\2\2\4R\3\2\2\2\6\\\3\2\2\2\b`\3\2\2\2\nm\3"+
		"\2\2\2\fo\3\2\2\2\16t\3\2\2\2\20w\3\2\2\2\22{\3\2\2\2\24\u0083\3\2\2\2"+
		"\26\u0086\3\2\2\2\30\u0092\3\2\2\2\32\u0096\3\2\2\2\34\u009a\3\2\2\2\36"+
		"\u009e\3\2\2\2 \u00a4\3\2\2\2\"\u00b5\3\2\2\2$\u00b7\3\2\2\2&\u00ca\3"+
		"\2\2\2(\u00d9\3\2\2\2*\u00db\3\2\2\2,\u00e7\3\2\2\2.\u00e9\3\2\2\2\60"+
		"\u00eb\3\2\2\2\62\u00fe\3\2\2\2\64\u0103\3\2\2\2\66\u0116\3\2\2\28\u0118"+
		"\3\2\2\2:\u012c\3\2\2\2<\u0142\3\2\2\2>\u014c\3\2\2\2@\u0151\3\2\2\2B"+
		"\u0166\3\2\2\2D\u0168\3\2\2\2F\u016c\3\2\2\2H\u016e\3\2\2\2J\u0170\3\2"+
		"\2\2L\u0176\3\2\2\2NO\5\b\5\2OP\7\2\2\3P\3\3\2\2\2QS\5\6\4\2RQ\3\2\2\2"+
		"RS\3\2\2\2ST\3\2\2\2TV\7\33\2\2UW\5<\37\2VU\3\2\2\2VW\3\2\2\2WX\3\2\2"+
		"\2XY\5:\36\2YZ\7\34\2\2Z[\7\2\2\3[\5\3\2\2\2\\]\7 \2\2]^\5L\'\2^_\7!\2"+
		"\2_\7\3\2\2\2`a\b\5\1\2ab\5\n\6\2bg\3\2\2\2cd\f\3\2\2df\5\n\6\2ec\3\2"+
		"\2\2fi\3\2\2\2ge\3\2\2\2gh\3\2\2\2h\t\3\2\2\2ig\3\2\2\2jn\5\26\f\2kn\5"+
		"\16\b\2ln\5\f\7\2mj\3\2\2\2mk\3\2\2\2ml\3\2\2\2n\13\3\2\2\2op\7\4\2\2"+
		"pq\7\31\2\2qr\5D#\2rs\7\32\2\2s\r\3\2\2\2tu\5\20\t\2uv\5*\26\2v\17\3\2"+
		"\2\2wx\7\n\2\2xy\7(\2\2yz\5L\'\2z\21\3\2\2\2{|\7\n\2\2|}\7\31\2\2}\177"+
		"\5L\'\2~\u0080\5\24\13\2\177~\3\2\2\2\177\u0080\3\2\2\2\u0080\u0081\3"+
		"\2\2\2\u0081\u0082\7\32\2\2\u0082\23\3\2\2\2\u0083\u0084\7#\2\2\u0084"+
		"\u0085\5\"\22\2\u0085\25\3\2\2\2\u0086\u0088\5\30\r\2\u0087\u0089\5\32"+
		"\16\2\u0088\u0087\3\2\2\2\u0088\u0089\3\2\2\2\u0089\u008b\3\2\2\2\u008a"+
		"\u008c\5\36\20\2\u008b\u008a\3\2\2\2\u008b\u008c\3\2\2\2\u008c\u008e\3"+
		"\2\2\2\u008d\u008f\5 \21\2\u008e\u008d\3\2\2\2\u008e\u008f\3\2\2\2\u008f"+
		"\u0090\3\2\2\2\u0090\u0091\5*\26\2\u0091\27\3\2\2\2\u0092\u0093\7\5\2"+
		"\2\u0093\u0094\7(\2\2\u0094\u0095\5L\'\2\u0095\31\3\2\2\2\u0096\u0097"+
		"\7\6\2\2\u0097\u0098\7(\2\2\u0098\u0099\7\r\2\2\u0099\33\3\2\2\2\u009a"+
		"\u009b\7,\2\2\u009b\u009c\5\62\32\2\u009c\u009d\7,\2\2\u009d\35\3\2\2"+
		"\2\u009e\u009f\7\7\2\2\u009f\u00a2\7(\2\2\u00a0\u00a3\5D#\2\u00a1\u00a3"+
		"\5\34\17\2\u00a2\u00a0\3\2\2\2\u00a2\u00a1\3\2\2\2\u00a3\37\3\2\2\2\u00a4"+
		"\u00a5\7\b\2\2\u00a5\u00a6\7(\2\2\u00a6\u00a7\5\"\22\2\u00a7!\3\2\2\2"+
		"\u00a8\u00a9\7\33\2\2\u00a9\u00ae\5$\23\2\u00aa\u00ab\7#\2\2\u00ab\u00ad"+
		"\5$\23\2\u00ac\u00aa\3\2\2\2\u00ad\u00b0\3\2\2\2\u00ae\u00ac\3\2\2\2\u00ae"+
		"\u00af\3\2\2\2\u00af\u00b1\3\2\2\2\u00b0\u00ae\3\2\2\2\u00b1\u00b2\7\34"+
		"\2\2\u00b2\u00b6\3\2\2\2\u00b3\u00b4\7\33\2\2\u00b4\u00b6\7\34\2\2\u00b5"+
		"\u00a8\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b6#\3\2\2\2\u00b7\u00b8\5D#\2\u00b8"+
		"\u00b9\7$\2\2\u00b9\u00ba\5&\24\2\u00ba%\3\2\2\2\u00bb\u00cb\5D#\2\u00bc"+
		"\u00be\7%\2\2\u00bd\u00bc\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00bf\3\2"+
		"\2\2\u00bf\u00c1\7\60\2\2\u00c0\u00c2\7\61\2\2\u00c1\u00c0\3\2\2\2\u00c1"+
		"\u00c2\3\2\2\2\u00c2\u00c4\3\2\2\2\u00c3\u00c5\7\62\2\2\u00c4\u00c3\3"+
		"\2\2\2\u00c4\u00c5\3\2\2\2\u00c5\u00cb\3\2\2\2\u00c6\u00cb\5\"\22\2\u00c7"+
		"\u00cb\5(\25\2\u00c8\u00cb\7\r\2\2\u00c9\u00cb\7\16\2\2\u00ca\u00bb\3"+
		"\2\2\2\u00ca\u00bd\3\2\2\2\u00ca\u00c6\3\2\2\2\u00ca\u00c7\3\2\2\2\u00ca"+
		"\u00c8\3\2\2\2\u00ca\u00c9\3\2\2\2\u00cb\'\3\2\2\2\u00cc\u00cd\7 \2\2"+
		"\u00cd\u00d2\5&\24\2\u00ce\u00cf\7#\2\2\u00cf\u00d1\5&\24\2\u00d0\u00ce"+
		"\3\2\2\2\u00d1\u00d4\3\2\2\2\u00d2\u00d0\3\2\2\2\u00d2\u00d3\3\2\2\2\u00d3"+
		"\u00d5\3\2\2\2\u00d4\u00d2\3\2\2\2\u00d5\u00d6\7!\2\2\u00d6\u00da\3\2"+
		"\2\2\u00d7\u00d8\7 \2\2\u00d8\u00da\7!\2\2\u00d9\u00cc\3\2\2\2\u00d9\u00d7"+
		"\3\2\2\2\u00da)\3\2\2\2\u00db\u00dc\b\26\1\2\u00dc\u00dd\5,\27\2\u00dd"+
		"\u00e2\3\2\2\2\u00de\u00df\f\3\2\2\u00df\u00e1\5,\27\2\u00e0\u00de\3\2"+
		"\2\2\u00e1\u00e4\3\2\2\2\u00e2\u00e0\3\2\2\2\u00e2\u00e3\3\2\2\2\u00e3"+
		"+\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e5\u00e8\5\60\31\2\u00e6\u00e8\5\22\n"+
		"\2\u00e7\u00e5\3\2\2\2\u00e7\u00e6\3\2\2\2\u00e8-\3\2\2\2\u00e9\u00ea"+
		"\t\2\2\2\u00ea/\3\2\2\2\u00eb\u00ed\7\t\2\2\u00ec\u00ee\58\35\2\u00ed"+
		"\u00ec\3\2\2\2\u00ed\u00ee\3\2\2\2\u00ee\u00ef\3\2\2\2\u00ef\u00f8\5."+
		"\30\2\u00f0\u00f2\7\33\2\2\u00f1\u00f3\5<\37\2\u00f2\u00f1\3\2\2\2\u00f2"+
		"\u00f3\3\2\2\2\u00f3\u00f4\3\2\2\2\u00f4\u00f5\5:\36\2\u00f5\u00f6\7\34"+
		"\2\2\u00f6\u00f9\3\2\2\2\u00f7\u00f9\5\34\17\2\u00f8\u00f0\3\2\2\2\u00f8"+
		"\u00f7\3\2\2\2\u00f9\u00fb\3\2\2\2\u00fa\u00fc\5F$\2\u00fb\u00fa\3\2\2"+
		"\2\u00fb\u00fc\3\2\2\2\u00fc\61\3\2\2\2\u00fd\u00ff\5\64\33\2\u00fe\u00fd"+
		"\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\7\"\2\2\u0101"+
		"\u0102\5L\'\2\u0102\63\3\2\2\2\u0103\u0104\b\33\1\2\u0104\u0105\5\66\34"+
		"\2\u0105\u010b\3\2\2\2\u0106\u0107\f\3\2\2\u0107\u0108\7&\2\2\u0108\u010a"+
		"\5\66\34\2\u0109\u0106\3\2\2\2\u010a\u010d\3\2\2\2\u010b\u0109\3\2\2\2"+
		"\u010b\u010c\3\2\2\2\u010c\65\3\2\2\2\u010d\u010b\3\2\2\2\u010e\u0117"+
		"\5L\'\2\u010f\u0117\7\4\2\2\u0110\u0117\7\5\2\2\u0111\u0117\7\6\2\2\u0112"+
		"\u0117\7\7\2\2\u0113\u0117\7\b\2\2\u0114\u0117\7\t\2\2\u0115\u0117\7\n"+
		"\2\2\u0116\u010e\3\2\2\2\u0116\u010f\3\2\2\2\u0116\u0110\3\2\2\2\u0116"+
		"\u0111\3\2\2\2\u0116\u0112\3\2\2\2\u0116\u0113\3\2\2\2\u0116\u0114\3\2"+
		"\2\2\u0116\u0115\3\2\2\2\u0117\67\3\2\2\2\u0118\u0119\7\31\2\2\u0119\u011a"+
		"\5L\'\2\u011a\u011b\7\32\2\2\u011b9\3\2\2\2\u011c\u011d\b\36\1\2\u011d"+
		"\u011e\t\3\2\2\u011e\u012d\5:\36\f\u011f\u0120\7\31\2\2\u0120\u0121\5"+
		":\36\2\u0121\u0122\7\32\2\2\u0122\u012d\3\2\2\2\u0123\u012d\5B\"\2\u0124"+
		"\u0125\7\3\2\2\u0125\u0127\7\31\2\2\u0126\u0128\5@!\2\u0127\u0126\3\2"+
		"\2\2\u0127\u0128\3\2\2\2\u0128\u0129\3\2\2\2\u0129\u012d\7\32\2\2\u012a"+
		"\u012b\7.\2\2\u012b\u012d\5L\'\2\u012c\u011c\3\2\2\2\u012c\u011f\3\2\2"+
		"\2\u012c\u0123\3\2\2\2\u012c\u0124\3\2\2\2\u012c\u012a\3\2\2\2\u012d\u013f"+
		"\3\2\2\2\u012e\u012f\f\n\2\2\u012f\u0130\t\4\2\2\u0130\u013e\5:\36\13"+
		"\u0131\u0132\f\t\2\2\u0132\u0133\t\5\2\2\u0133\u013e\5:\36\n\u0134\u0135"+
		"\f\b\2\2\u0135\u0136\t\6\2\2\u0136\u013e\5:\36\t\u0137\u0138\f\7\2\2\u0138"+
		"\u0139\t\7\2\2\u0139\u013e\5:\36\b\u013a\u013b\f\6\2\2\u013b\u013c\t\b"+
		"\2\2\u013c\u013e\5:\36\7\u013d\u012e\3\2\2\2\u013d\u0131\3\2\2\2\u013d"+
		"\u0134\3\2\2\2\u013d\u0137\3\2\2\2\u013d\u013a\3\2\2\2\u013e\u0141\3\2"+
		"\2\2\u013f\u013d\3\2\2\2\u013f\u0140\3\2\2\2\u0140;\3\2\2\2\u0141\u013f"+
		"\3\2\2\2\u0142\u0143\b\37\1\2\u0143\u0144\5> \2\u0144\u0149\3\2\2\2\u0145"+
		"\u0146\f\3\2\2\u0146\u0148\5> \2\u0147\u0145\3\2\2\2\u0148\u014b\3\2\2"+
		"\2\u0149\u0147\3\2\2\2\u0149\u014a\3\2\2\2\u014a=\3\2\2\2\u014b\u0149"+
		"\3\2\2\2\u014c\u014d\7.\2\2\u014d\u014e\5L\'\2\u014e\u014f\7(\2\2\u014f"+
		"\u0150\5:\36\2\u0150?\3\2\2\2\u0151\u0152\b!\1\2\u0152\u0153\5:\36\2\u0153"+
		"\u0159\3\2\2\2\u0154\u0155\f\3\2\2\u0155\u0156\7#\2\2\u0156\u0158\5:\36"+
		"\2\u0157\u0154\3\2\2\2\u0158\u015b\3\2\2\2\u0159\u0157\3\2\2\2\u0159\u015a"+
		"\3\2\2\2\u015aA\3\2\2\2\u015b\u0159\3\2\2\2\u015c\u0167\7\16\2\2\u015d"+
		"\u015f\7\60\2\2\u015e\u0160\7\61\2\2\u015f\u015e\3\2\2\2\u015f\u0160\3"+
		"\2\2\2\u0160\u0162\3\2\2\2\u0161\u0163\7\62\2\2\u0162\u0161\3\2\2\2\u0162"+
		"\u0163\3\2\2\2\u0163\u0167\3\2\2\2\u0164\u0167\7\r\2\2\u0165\u0167\5D"+
		"#\2\u0166\u015c\3\2\2\2\u0166\u015d\3\2\2\2\u0166\u0164\3\2\2\2\u0166"+
		"\u0165\3\2\2\2\u0167C\3\2\2\2\u0168\u0169\t\t\2\2\u0169E\3\2\2\2\u016a"+
		"\u016d\5H%\2\u016b\u016d\5J&\2\u016c\u016a\3\2\2\2\u016c\u016b\3\2\2\2"+
		"\u016dG\3\2\2\2\u016e\u016f\t\n\2\2\u016fI\3\2\2\2\u0170\u0171\7 \2\2"+
		"\u0171\u0172\7\60\2\2\u0172\u0173\7#\2\2\u0173\u0174\7\60\2\2\u0174\u0175"+
		"\7!\2\2\u0175K\3\2\2\2\u0176\u0177\t\13\2\2\u0177M\3\2\2\2&RVgm\177\u0088"+
		"\u008b\u008e\u00a2\u00ae\u00b5\u00bd\u00c1\u00c4\u00ca\u00d2\u00d9\u00e2"+
		"\u00e7\u00ed\u00f2\u00f8\u00fb\u00fe\u010b\u0116\u0127\u012c\u013d\u013f"+
		"\u0149\u0159\u015f\u0162\u0166\u016c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}