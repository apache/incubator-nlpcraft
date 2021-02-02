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
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, SQSTRING=15, DQSTRING=16, 
		PRED_OP=17, AND=18, OR=19, VERT=20, EXCL=21, LPAREN=22, RPAREN=23, LCURLY=24, 
		RCURLY=25, SQUOTE=26, DQUOTE=27, TILDA=28, RIGHT=29, LBR=30, RBR=31, POUND=32, 
		COMMA=33, COLON=34, MINUS=35, DOT=36, UNDERSCORE=37, EQ=38, PLUS=39, QUESTION=40, 
		STAR=41, DEVIDE=42, DOLLAR=43, POWER=44, BOOL=45, INT=46, EXP=47, ID=48, 
		WS=49, ErrorCharacter=50;
	public static final int
		RULE_intent = 0, RULE_intentId = 1, RULE_orderedDecl = 2, RULE_flowDecl = 3, 
		RULE_metaDecl = 4, RULE_metaList = 5, RULE_metaItem = 6, RULE_metaItemRval = 7, 
		RULE_terms = 8, RULE_termEq = 9, RULE_term = 10, RULE_termId = 11, RULE_item = 12, 
		RULE_pred = 13, RULE_expr = 14, RULE_mathOp = 15, RULE_val = 16, RULE_singleVal = 17, 
		RULE_tokQual = 18, RULE_tokQualPart = 19, RULE_tokMeta = 20, RULE_modelMeta = 21, 
		RULE_intentMeta = 22, RULE_qstring = 23, RULE_minMax = 24, RULE_minMaxShortcut = 25, 
		RULE_minMaxRange = 26;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "intentId", "orderedDecl", "flowDecl", "metaDecl", "metaList", 
			"metaItem", "metaItemRval", "terms", "termEq", "term", "termId", "item", 
			"pred", "expr", "mathOp", "val", "singleVal", "tokQual", "tokQualPart", 
			"tokMeta", "modelMeta", "intentMeta", "qstring", "minMax", "minMaxShortcut", 
			"minMaxRange"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'meta'", "'null'", "'term'", 
			"'id'", "'aliases'", "'startidx'", "'endidx'", "'parent'", "'groups'", 
			"'ancestors'", "'value'", null, null, null, "'&&'", "'||'", "'|'", "'!'", 
			"'('", "')'", "'{'", "'}'", "'''", "'\"'", "'~'", "'>>'", "'['", "']'", 
			"'#'", "','", "':'", "'-'", "'.'", "'_'", "'='", "'+'", "'?'", "'*'", 
			"'/'", "'$'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, "SQSTRING", "DQSTRING", "PRED_OP", "AND", "OR", "VERT", 
			"EXCL", "LPAREN", "RPAREN", "LCURLY", "RCURLY", "SQUOTE", "DQUOTE", "TILDA", 
			"RIGHT", "LBR", "RBR", "POUND", "COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", 
			"EQ", "PLUS", "QUESTION", "STAR", "DEVIDE", "DOLLAR", "POWER", "BOOL", 
			"INT", "EXP", "ID", "WS", "ErrorCharacter"
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
			setState(54);
			intentId();
			setState(56);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(55);
				orderedDecl();
				}
			}

			setState(59);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(58);
				flowDecl();
				}
			}

			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(61);
				metaDecl();
				}
			}

			setState(64);
			terms(0);
			setState(65);
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
			setState(67);
			match(T__0);
			setState(68);
			match(EQ);
			setState(69);
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
			setState(71);
			match(T__1);
			setState(72);
			match(EQ);
			setState(73);
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
			setState(75);
			match(T__2);
			setState(76);
			match(EQ);
			setState(77);
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
		public TerminalNode LCURLY() { return getToken(NCIntentDslParser.LCURLY, 0); }
		public TerminalNode RCURLY() { return getToken(NCIntentDslParser.RCURLY, 0); }
		public MetaListContext metaList() {
			return getRuleContext(MetaListContext.class,0);
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
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(79);
			match(T__3);
			setState(80);
			match(EQ);
			setState(81);
			match(LCURLY);
			setState(83);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==SQSTRING || _la==DQSTRING) {
				{
				setState(82);
				metaList(0);
				}
			}

			setState(85);
			match(RCURLY);
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

	public static class MetaListContext extends ParserRuleContext {
		public MetaItemContext metaItem() {
			return getRuleContext(MetaItemContext.class,0);
		}
		public MetaListContext metaList() {
			return getRuleContext(MetaListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public MetaListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMetaList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMetaList(this);
		}
	}

	public final MetaListContext metaList() throws RecognitionException {
		return metaList(0);
	}

	private MetaListContext metaList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		MetaListContext _localctx = new MetaListContext(_ctx, _parentState);
		MetaListContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_metaList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(88);
			metaItem();
			}
			_ctx.stop = _input.LT(-1);
			setState(95);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new MetaListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_metaList);
					setState(90);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(91);
					match(COMMA);
					setState(92);
					metaItem();
					}
					} 
				}
				setState(97);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
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

	public static class MetaItemContext extends ParserRuleContext {
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TerminalNode COLON() { return getToken(NCIntentDslParser.COLON, 0); }
		public MetaItemRvalContext metaItemRval() {
			return getRuleContext(MetaItemRvalContext.class,0);
		}
		public MetaItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMetaItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMetaItem(this);
		}
	}

	public final MetaItemContext metaItem() throws RecognitionException {
		MetaItemContext _localctx = new MetaItemContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_metaItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(98);
			qstring();
			setState(99);
			match(COLON);
			setState(100);
			metaItemRval();
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

	public static class MetaItemRvalContext extends ParserRuleContext {
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public MetaItemRvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_metaItemRval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMetaItemRval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMetaItemRval(this);
		}
	}

	public final MetaItemRvalContext metaItemRval() throws RecognitionException {
		MetaItemRvalContext _localctx = new MetaItemRvalContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_metaItemRval);
		try {
			setState(105);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				enterOuterAlt(_localctx, 1);
				{
				setState(102);
				match(T__4);
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 2);
				{
				setState(103);
				match(BOOL);
				}
				break;
			case SQSTRING:
			case DQSTRING:
				enterOuterAlt(_localctx, 3);
				{
				setState(104);
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
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_terms, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(108);
			term();
			}
			_ctx.stop = _input.LT(-1);
			setState(114);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_terms);
					setState(110);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(111);
					term();
					}
					} 
				}
				setState(116);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
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
		enterRule(_localctx, 18, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117);
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
		public TerminalNode LCURLY() { return getToken(NCIntentDslParser.LCURLY, 0); }
		public ItemContext item() {
			return getRuleContext(ItemContext.class,0);
		}
		public TerminalNode RCURLY() { return getToken(NCIntentDslParser.RCURLY, 0); }
		public TermIdContext termId() {
			return getRuleContext(TermIdContext.class,0);
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
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(T__5);
			setState(121);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(120);
				termId();
				}
			}

			setState(123);
			termEq();
			setState(124);
			match(LCURLY);
			setState(125);
			item(0);
			setState(126);
			match(RCURLY);
			setState(128);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				{
				setState(127);
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
		enterRule(_localctx, 22, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(130);
			match(LPAREN);
			setState(131);
			match(ID);
			setState(132);
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

	public static class ItemContext extends ParserRuleContext {
		public PredContext pred() {
			return getRuleContext(PredContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public List<ItemContext> item() {
			return getRuleContexts(ItemContext.class);
		}
		public ItemContext item(int i) {
			return getRuleContext(ItemContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TerminalNode EXCL() { return getToken(NCIntentDslParser.EXCL, 0); }
		public TerminalNode AND() { return getToken(NCIntentDslParser.AND, 0); }
		public TerminalNode OR() { return getToken(NCIntentDslParser.OR, 0); }
		public ItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_item; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitItem(this);
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
		int _startState = 24;
		enterRecursionRule(_localctx, 24, RULE_item, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(142);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				{
				setState(135);
				pred();
				}
				break;
			case 2:
				{
				setState(136);
				match(LPAREN);
				setState(137);
				item(0);
				setState(138);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(140);
				match(EXCL);
				setState(141);
				item(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(149);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ItemContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_item);
					setState(144);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(145);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(146);
					item(3);
					}
					} 
				}
				setState(151);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
		public TerminalNode PRED_OP() { return getToken(NCIntentDslParser.PRED_OP, 0); }
		public PredContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_pred; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterPred(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitPred(this);
		}
	}

	public final PredContext pred() throws RecognitionException {
		PredContext _localctx = new PredContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_pred);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(152);
			expr(0);
			setState(153);
			match(PRED_OP);
			setState(154);
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
		public MathOpContext mathOp() {
			return getRuleContext(MathOpContext.class,0);
		}
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
		int _startState = 28;
		enterRecursionRule(_localctx, 28, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(168);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				{
				setState(157);
				val(0);
				}
				break;
			case 2:
				{
				setState(158);
				match(LPAREN);
				setState(159);
				expr(0);
				setState(160);
				match(RPAREN);
				}
				break;
			case 3:
				{
				setState(162);
				match(ID);
				setState(163);
				match(LPAREN);
				setState(165);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__4) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << SQSTRING) | (1L << DQSTRING) | (1L << LPAREN) | (1L << TILDA) | (1L << POUND) | (1L << MINUS) | (1L << DOLLAR) | (1L << BOOL) | (1L << INT) | (1L << ID))) != 0)) {
					{
					setState(164);
					expr(0);
					}
				}

				setState(167);
				match(RPAREN);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(176);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ExprContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_expr);
					setState(170);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(171);
					mathOp();
					setState(172);
					expr(3);
					}
					} 
				}
				setState(178);
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

	public static class MathOpContext extends ParserRuleContext {
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode PLUS() { return getToken(NCIntentDslParser.PLUS, 0); }
		public TerminalNode STAR() { return getToken(NCIntentDslParser.STAR, 0); }
		public TerminalNode DEVIDE() { return getToken(NCIntentDslParser.DEVIDE, 0); }
		public MathOpContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_mathOp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMathOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMathOp(this);
		}
	}

	public final MathOpContext mathOp() throws RecognitionException {
		MathOpContext _localctx = new MathOpContext(_ctx, getState());
		enterRule(_localctx, 30, RULE_mathOp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(179);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << MINUS) | (1L << PLUS) | (1L << STAR) | (1L << DEVIDE))) != 0)) ) {
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
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public List<ValContext> val() {
			return getRuleContexts(ValContext.class);
		}
		public ValContext val(int i) {
			return getRuleContext(ValContext.class,i);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
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
		return val(0);
	}

	private ValContext val(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ValContext _localctx = new ValContext(_ctx, _parentState);
		ValContext _prevctx = _localctx;
		int _startState = 32;
		enterRecursionRule(_localctx, 32, RULE_val, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(187);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case T__12:
			case T__13:
			case SQSTRING:
			case DQSTRING:
			case TILDA:
			case POUND:
			case MINUS:
			case DOLLAR:
			case BOOL:
			case INT:
			case ID:
				{
				setState(182);
				singleVal();
				}
				break;
			case LPAREN:
				{
				setState(183);
				match(LPAREN);
				setState(184);
				val(0);
				setState(185);
				match(RPAREN);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(194);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ValContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_val);
					setState(189);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(190);
					match(COMMA);
					setState(191);
					val(2);
					}
					} 
				}
				setState(196);
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

	public static class SingleValContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode EXP() { return getToken(NCIntentDslParser.EXP, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
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
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterSingleVal(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitSingleVal(this);
		}
	}

	public final SingleValContext singleVal() throws RecognitionException {
		SingleValContext _localctx = new SingleValContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_singleVal);
		int _la;
		try {
			setState(218);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,20,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(197);
				match(T__4);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(199);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(198);
					match(MINUS);
					}
				}

				setState(204);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
				case 1:
					{
					setState(201);
					match(INT);
					}
					break;
				case 2:
					{
					setState(202);
					match(INT);
					setState(203);
					match(EXP);
					}
					break;
				}
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(206);
				match(BOOL);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(207);
				qstring();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(208);
					tokQual(0);
					}
				}

				setState(211);
				_la = _input.LA(1);
				if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13))) != 0)) ) {
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
				setState(213);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==ID) {
					{
					setState(212);
					tokQual(0);
					}
				}

				setState(215);
				tokMeta();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(216);
				modelMeta();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(217);
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
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTokQual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTokQual(this);
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
		int _startState = 36;
		enterRecursionRule(_localctx, 36, RULE_tokQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(221);
			tokQualPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(227);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TokQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_tokQual);
					setState(223);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(224);
					tokQualPart();
					}
					} 
				}
				setState(229);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
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
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode DOT() { return getToken(NCIntentDslParser.DOT, 0); }
		public TokQualPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokQualPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTokQualPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTokQualPart(this);
		}
	}

	public final TokQualPartContext tokQualPart() throws RecognitionException {
		TokQualPartContext _localctx = new TokQualPartContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_tokQualPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(230);
			match(ID);
			setState(231);
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
		public TerminalNode TILDA() { return getToken(NCIntentDslParser.TILDA, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public TokMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterTokMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitTokMeta(this);
		}
	}

	public final TokMetaContext tokMeta() throws RecognitionException {
		TokMetaContext _localctx = new TokMetaContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_tokMeta);
		try {
			setState(246);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(233);
				match(TILDA);
				setState(234);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(235);
				match(TILDA);
				setState(236);
				match(ID);
				setState(237);
				match(LBR);
				setState(238);
				match(INT);
				setState(239);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(240);
				match(TILDA);
				setState(241);
				match(ID);
				setState(242);
				match(LBR);
				setState(243);
				qstring();
				setState(244);
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
		public TerminalNode POUND() { return getToken(NCIntentDslParser.POUND, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public ModelMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_modelMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterModelMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitModelMeta(this);
		}
	}

	public final ModelMetaContext modelMeta() throws RecognitionException {
		ModelMetaContext _localctx = new ModelMetaContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_modelMeta);
		try {
			setState(261);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(248);
				match(POUND);
				setState(249);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(250);
				match(POUND);
				setState(251);
				match(ID);
				setState(252);
				match(LBR);
				setState(253);
				match(INT);
				setState(254);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(255);
				match(POUND);
				setState(256);
				match(ID);
				setState(257);
				match(LBR);
				setState(258);
				qstring();
				setState(259);
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
		public TerminalNode DOLLAR() { return getToken(NCIntentDslParser.DOLLAR, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public IntentMetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_intentMeta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIntentMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIntentMeta(this);
		}
	}

	public final IntentMetaContext intentMeta() throws RecognitionException {
		IntentMetaContext _localctx = new IntentMetaContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_intentMeta);
		try {
			setState(276);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,24,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(263);
				match(DOLLAR);
				setState(264);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(265);
				match(DOLLAR);
				setState(266);
				match(ID);
				setState(267);
				match(LBR);
				setState(268);
				match(INT);
				setState(269);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(270);
				match(DOLLAR);
				setState(271);
				match(ID);
				setState(272);
				match(LBR);
				setState(273);
				qstring();
				setState(274);
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
		enterRule(_localctx, 46, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
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
		enterRule(_localctx, 48, RULE_minMax);
		try {
			setState(282);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(280);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(281);
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
		enterRule(_localctx, 50, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(284);
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
		enterRule(_localctx, 52, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(286);
			match(LBR);
			setState(287);
			match(INT);
			setState(288);
			match(COMMA);
			setState(289);
			match(INT);
			setState(290);
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
		case 5:
			return metaList_sempred((MetaListContext)_localctx, predIndex);
		case 8:
			return terms_sempred((TermsContext)_localctx, predIndex);
		case 12:
			return item_sempred((ItemContext)_localctx, predIndex);
		case 14:
			return expr_sempred((ExprContext)_localctx, predIndex);
		case 16:
			return val_sempred((ValContext)_localctx, predIndex);
		case 18:
			return tokQual_sempred((TokQualContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean metaList_sempred(MetaListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean terms_sempred(TermsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean item_sempred(ItemContext _localctx, int predIndex) {
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
		}
		return true;
	}
	private boolean val_sempred(ValContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean tokQual_sempred(TokQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\64\u0127\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\3\2\3\2\5\2;\n\2\3\2\5\2>\n\2\3\2\5\2A"+
		"\n\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3"+
		"\6\3\6\3\6\5\6V\n\6\3\6\3\6\3\7\3\7\3\7\3\7\3\7\3\7\7\7`\n\7\f\7\16\7"+
		"c\13\7\3\b\3\b\3\b\3\b\3\t\3\t\3\t\5\tl\n\t\3\n\3\n\3\n\3\n\3\n\7\ns\n"+
		"\n\f\n\16\nv\13\n\3\13\3\13\3\f\3\f\5\f|\n\f\3\f\3\f\3\f\3\f\3\f\5\f\u0083"+
		"\n\f\3\r\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0091"+
		"\n\16\3\16\3\16\3\16\7\16\u0096\n\16\f\16\16\16\u0099\13\16\3\17\3\17"+
		"\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u00a8\n\20"+
		"\3\20\5\20\u00ab\n\20\3\20\3\20\3\20\3\20\7\20\u00b1\n\20\f\20\16\20\u00b4"+
		"\13\20\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\5\22\u00be\n\22\3\22\3"+
		"\22\3\22\7\22\u00c3\n\22\f\22\16\22\u00c6\13\22\3\23\3\23\5\23\u00ca\n"+
		"\23\3\23\3\23\3\23\5\23\u00cf\n\23\3\23\3\23\3\23\5\23\u00d4\n\23\3\23"+
		"\3\23\5\23\u00d8\n\23\3\23\3\23\3\23\5\23\u00dd\n\23\3\24\3\24\3\24\3"+
		"\24\3\24\7\24\u00e4\n\24\f\24\16\24\u00e7\13\24\3\25\3\25\3\25\3\26\3"+
		"\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u00f9"+
		"\n\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27"+
		"\5\27\u0108\n\27\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30\3\30"+
		"\3\30\3\30\5\30\u0117\n\30\3\31\3\31\3\32\3\32\5\32\u011d\n\32\3\33\3"+
		"\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\2\b\f\22\32\36\"&\35\2\4\6\b\n"+
		"\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\66\2\b\4\2\36\36((\3\2"+
		"\24\25\5\2%%))+,\3\2\t\20\3\2\21\22\3\2)+\2\u0131\28\3\2\2\2\4E\3\2\2"+
		"\2\6I\3\2\2\2\bM\3\2\2\2\nQ\3\2\2\2\fY\3\2\2\2\16d\3\2\2\2\20k\3\2\2\2"+
		"\22m\3\2\2\2\24w\3\2\2\2\26y\3\2\2\2\30\u0084\3\2\2\2\32\u0090\3\2\2\2"+
		"\34\u009a\3\2\2\2\36\u00aa\3\2\2\2 \u00b5\3\2\2\2\"\u00bd\3\2\2\2$\u00dc"+
		"\3\2\2\2&\u00de\3\2\2\2(\u00e8\3\2\2\2*\u00f8\3\2\2\2,\u0107\3\2\2\2."+
		"\u0116\3\2\2\2\60\u0118\3\2\2\2\62\u011c\3\2\2\2\64\u011e\3\2\2\2\66\u0120"+
		"\3\2\2\28:\5\4\3\29;\5\6\4\2:9\3\2\2\2:;\3\2\2\2;=\3\2\2\2<>\5\b\5\2="+
		"<\3\2\2\2=>\3\2\2\2>@\3\2\2\2?A\5\n\6\2@?\3\2\2\2@A\3\2\2\2AB\3\2\2\2"+
		"BC\5\22\n\2CD\7\2\2\3D\3\3\2\2\2EF\7\3\2\2FG\7(\2\2GH\7\62\2\2H\5\3\2"+
		"\2\2IJ\7\4\2\2JK\7(\2\2KL\7/\2\2L\7\3\2\2\2MN\7\5\2\2NO\7(\2\2OP\5\60"+
		"\31\2P\t\3\2\2\2QR\7\6\2\2RS\7(\2\2SU\7\32\2\2TV\5\f\7\2UT\3\2\2\2UV\3"+
		"\2\2\2VW\3\2\2\2WX\7\33\2\2X\13\3\2\2\2YZ\b\7\1\2Z[\5\16\b\2[a\3\2\2\2"+
		"\\]\f\3\2\2]^\7#\2\2^`\5\16\b\2_\\\3\2\2\2`c\3\2\2\2a_\3\2\2\2ab\3\2\2"+
		"\2b\r\3\2\2\2ca\3\2\2\2de\5\60\31\2ef\7$\2\2fg\5\20\t\2g\17\3\2\2\2hl"+
		"\7\7\2\2il\7/\2\2jl\5\60\31\2kh\3\2\2\2ki\3\2\2\2kj\3\2\2\2l\21\3\2\2"+
		"\2mn\b\n\1\2no\5\26\f\2ot\3\2\2\2pq\f\3\2\2qs\5\26\f\2rp\3\2\2\2sv\3\2"+
		"\2\2tr\3\2\2\2tu\3\2\2\2u\23\3\2\2\2vt\3\2\2\2wx\t\2\2\2x\25\3\2\2\2y"+
		"{\7\b\2\2z|\5\30\r\2{z\3\2\2\2{|\3\2\2\2|}\3\2\2\2}~\5\24\13\2~\177\7"+
		"\32\2\2\177\u0080\5\32\16\2\u0080\u0082\7\33\2\2\u0081\u0083\5\62\32\2"+
		"\u0082\u0081\3\2\2\2\u0082\u0083\3\2\2\2\u0083\27\3\2\2\2\u0084\u0085"+
		"\7\30\2\2\u0085\u0086\7\62\2\2\u0086\u0087\7\31\2\2\u0087\31\3\2\2\2\u0088"+
		"\u0089\b\16\1\2\u0089\u0091\5\34\17\2\u008a\u008b\7\30\2\2\u008b\u008c"+
		"\5\32\16\2\u008c\u008d\7\31\2\2\u008d\u0091\3\2\2\2\u008e\u008f\7\27\2"+
		"\2\u008f\u0091\5\32\16\3\u0090\u0088\3\2\2\2\u0090\u008a\3\2\2\2\u0090"+
		"\u008e\3\2\2\2\u0091\u0097\3\2\2\2\u0092\u0093\f\4\2\2\u0093\u0094\t\3"+
		"\2\2\u0094\u0096\5\32\16\5\u0095\u0092\3\2\2\2\u0096\u0099\3\2\2\2\u0097"+
		"\u0095\3\2\2\2\u0097\u0098\3\2\2\2\u0098\33\3\2\2\2\u0099\u0097\3\2\2"+
		"\2\u009a\u009b\5\36\20\2\u009b\u009c\7\23\2\2\u009c\u009d\5\36\20\2\u009d"+
		"\35\3\2\2\2\u009e\u009f\b\20\1\2\u009f\u00ab\5\"\22\2\u00a0\u00a1\7\30"+
		"\2\2\u00a1\u00a2\5\36\20\2\u00a2\u00a3\7\31\2\2\u00a3\u00ab\3\2\2\2\u00a4"+
		"\u00a5\7\62\2\2\u00a5\u00a7\7\30\2\2\u00a6\u00a8\5\36\20\2\u00a7\u00a6"+
		"\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00ab\7\31\2\2"+
		"\u00aa\u009e\3\2\2\2\u00aa\u00a0\3\2\2\2\u00aa\u00a4\3\2\2\2\u00ab\u00b2"+
		"\3\2\2\2\u00ac\u00ad\f\4\2\2\u00ad\u00ae\5 \21\2\u00ae\u00af\5\36\20\5"+
		"\u00af\u00b1\3\2\2\2\u00b0\u00ac\3\2\2\2\u00b1\u00b4\3\2\2\2\u00b2\u00b0"+
		"\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\37\3\2\2\2\u00b4\u00b2\3\2\2\2\u00b5"+
		"\u00b6\t\4\2\2\u00b6!\3\2\2\2\u00b7\u00b8\b\22\1\2\u00b8\u00be\5$\23\2"+
		"\u00b9\u00ba\7\30\2\2\u00ba\u00bb\5\"\22\2\u00bb\u00bc\7\31\2\2\u00bc"+
		"\u00be\3\2\2\2\u00bd\u00b7\3\2\2\2\u00bd\u00b9\3\2\2\2\u00be\u00c4\3\2"+
		"\2\2\u00bf\u00c0\f\3\2\2\u00c0\u00c1\7#\2\2\u00c1\u00c3\5\"\22\4\u00c2"+
		"\u00bf\3\2\2\2\u00c3\u00c6\3\2\2\2\u00c4\u00c2\3\2\2\2\u00c4\u00c5\3\2"+
		"\2\2\u00c5#\3\2\2\2\u00c6\u00c4\3\2\2\2\u00c7\u00dd\7\7\2\2\u00c8\u00ca"+
		"\7%\2\2\u00c9\u00c8\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\u00ce\3\2\2\2\u00cb"+
		"\u00cf\7\60\2\2\u00cc\u00cd\7\60\2\2\u00cd\u00cf\7\61\2\2\u00ce\u00cb"+
		"\3\2\2\2\u00ce\u00cc\3\2\2\2\u00cf\u00dd\3\2\2\2\u00d0\u00dd\7/\2\2\u00d1"+
		"\u00dd\5\60\31\2\u00d2\u00d4\5&\24\2\u00d3\u00d2\3\2\2\2\u00d3\u00d4\3"+
		"\2\2\2\u00d4\u00d5\3\2\2\2\u00d5\u00dd\t\5\2\2\u00d6\u00d8\5&\24\2\u00d7"+
		"\u00d6\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00dd\5*"+
		"\26\2\u00da\u00dd\5,\27\2\u00db\u00dd\5.\30\2\u00dc\u00c7\3\2\2\2\u00dc"+
		"\u00c9\3\2\2\2\u00dc\u00d0\3\2\2\2\u00dc\u00d1\3\2\2\2\u00dc\u00d3\3\2"+
		"\2\2\u00dc\u00d7\3\2\2\2\u00dc\u00da\3\2\2\2\u00dc\u00db\3\2\2\2\u00dd"+
		"%\3\2\2\2\u00de\u00df\b\24\1\2\u00df\u00e0\5(\25\2\u00e0\u00e5\3\2\2\2"+
		"\u00e1\u00e2\f\3\2\2\u00e2\u00e4\5(\25\2\u00e3\u00e1\3\2\2\2\u00e4\u00e7"+
		"\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\'\3\2\2\2\u00e7"+
		"\u00e5\3\2\2\2\u00e8\u00e9\7\62\2\2\u00e9\u00ea\7&\2\2\u00ea)\3\2\2\2"+
		"\u00eb\u00ec\7\36\2\2\u00ec\u00f9\7\62\2\2\u00ed\u00ee\7\36\2\2\u00ee"+
		"\u00ef\7\62\2\2\u00ef\u00f0\7 \2\2\u00f0\u00f1\7\60\2\2\u00f1\u00f9\7"+
		"!\2\2\u00f2\u00f3\7\36\2\2\u00f3\u00f4\7\62\2\2\u00f4\u00f5\7 \2\2\u00f5"+
		"\u00f6\5\60\31\2\u00f6\u00f7\7!\2\2\u00f7\u00f9\3\2\2\2\u00f8\u00eb\3"+
		"\2\2\2\u00f8\u00ed\3\2\2\2\u00f8\u00f2\3\2\2\2\u00f9+\3\2\2\2\u00fa\u00fb"+
		"\7\"\2\2\u00fb\u0108\7\62\2\2\u00fc\u00fd\7\"\2\2\u00fd\u00fe\7\62\2\2"+
		"\u00fe\u00ff\7 \2\2\u00ff\u0100\7\60\2\2\u0100\u0108\7!\2\2\u0101\u0102"+
		"\7\"\2\2\u0102\u0103\7\62\2\2\u0103\u0104\7 \2\2\u0104\u0105\5\60\31\2"+
		"\u0105\u0106\7!\2\2\u0106\u0108\3\2\2\2\u0107\u00fa\3\2\2\2\u0107\u00fc"+
		"\3\2\2\2\u0107\u0101\3\2\2\2\u0108-\3\2\2\2\u0109\u010a\7-\2\2\u010a\u0117"+
		"\7\62\2\2\u010b\u010c\7-\2\2\u010c\u010d\7\62\2\2\u010d\u010e\7 \2\2\u010e"+
		"\u010f\7\60\2\2\u010f\u0117\7!\2\2\u0110\u0111\7-\2\2\u0111\u0112\7\62"+
		"\2\2\u0112\u0113\7 \2\2\u0113\u0114\5\60\31\2\u0114\u0115\7!\2\2\u0115"+
		"\u0117\3\2\2\2\u0116\u0109\3\2\2\2\u0116\u010b\3\2\2\2\u0116\u0110\3\2"+
		"\2\2\u0117/\3\2\2\2\u0118\u0119\t\6\2\2\u0119\61\3\2\2\2\u011a\u011d\5"+
		"\64\33\2\u011b\u011d\5\66\34\2\u011c\u011a\3\2\2\2\u011c\u011b\3\2\2\2"+
		"\u011d\63\3\2\2\2\u011e\u011f\t\7\2\2\u011f\65\3\2\2\2\u0120\u0121\7 "+
		"\2\2\u0121\u0122\7\60\2\2\u0122\u0123\7#\2\2\u0123\u0124\7\60\2\2\u0124"+
		"\u0125\7!\2\2\u0125\67\3\2\2\2\34:=@Uakt{\u0082\u0090\u0097\u00a7\u00aa"+
		"\u00b2\u00bd\u00c4\u00c9\u00ce\u00d3\u00d7\u00dc\u00e5\u00f8\u0107\u0116"+
		"\u011c";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}