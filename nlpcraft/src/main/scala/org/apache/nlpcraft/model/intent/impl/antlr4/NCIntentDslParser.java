// Generated from C:/Users/Nikita Ivanov/Documents/GitHub/incubator-nlpcraft/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4\NCIntentDsl.g4 by ANTLR 4.9
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
	static { RuntimeMetaData.checkVersion("4.9", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, QSTRING=14, PRED_OP=15, AND=16, 
		OR=17, VERT=18, EXCL=19, LPAREN=20, RPAREN=21, LCURLY=22, RCURLY=23, SQUOTE=24, 
		TILDA=25, RIGHT=26, LBR=27, RBR=28, COMMA=29, COLON=30, MINUS=31, DOT=32, 
		UNDERSCORE=33, EQ=34, PLUS=35, QUESTION=36, STAR=37, DOLLAR=38, POWER=39, 
		BOOL=40, INT=41, EXP=42, ID=43, WS=44, ErrorCharacter=45;
	public static final int
		RULE_intent = 0, RULE_intentId = 1, RULE_orderedDecl = 2, RULE_flowDecl = 3, 
		RULE_terms = 4, RULE_termEq = 5, RULE_term = 6, RULE_termId = 7, RULE_item = 8, 
		RULE_predicate = 9, RULE_lval = 10, RULE_lvalQual = 11, RULE_lvalPart = 12, 
		RULE_rvalSingle = 13, RULE_rval = 14, RULE_rvalList = 15, RULE_meta = 16, 
		RULE_qstring = 17, RULE_minMax = 18, RULE_minMaxShortcut = 19, RULE_minMaxRange = 20;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "intentId", "orderedDecl", "flowDecl", "terms", "termEq", "term", 
			"termId", "item", "predicate", "lval", "lvalQual", "lvalPart", "rvalSingle", 
			"rval", "rvalList", "meta", "qstring", "minMax", "minMaxShortcut", "minMaxRange"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'ordered'", "'flow'", "'term'", "'id'", "'aliases'", 
			"'startidx'", "'endidx'", "'parent'", "'groups'", "'ancestors'", "'value'", 
			"'null'", null, null, "'&&'", "'||'", "'|'", "'!'", "'('", "')'", "'{'", 
			"'}'", "'''", "'~'", "'>>'", "'['", "']'", "','", "':'", "'-'", "'.'", 
			"'_'", "'='", "'+'", "'?'", "'*'", "'$'", "'^'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, "QSTRING", "PRED_OP", "AND", "OR", "VERT", "EXCL", "LPAREN", 
			"RPAREN", "LCURLY", "RCURLY", "SQUOTE", "TILDA", "RIGHT", "LBR", "RBR", 
			"COMMA", "COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", 
			"STAR", "DOLLAR", "POWER", "BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
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

			setState(49);
			terms(0);
			setState(50);
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
			setState(52);
			match(T__0);
			setState(53);
			match(EQ);
			setState(54);
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
			setState(56);
			match(T__1);
			setState(57);
			match(EQ);
			setState(58);
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
			setState(60);
			match(T__2);
			setState(61);
			match(EQ);
			setState(62);
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
		int _startState = 8;
		enterRecursionRule(_localctx, 8, RULE_terms, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(65);
			term();
			}
			_ctx.stop = _input.LT(-1);
			setState(71);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_terms);
					setState(67);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(68);
					term();
					}
					} 
				}
				setState(73);
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
		enterRule(_localctx, 10, RULE_termEq);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(74);
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
		enterRule(_localctx, 12, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(76);
			match(T__3);
			setState(78);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(77);
				termId();
				}
			}

			setState(80);
			termEq();
			setState(81);
			match(LCURLY);
			setState(82);
			item(0);
			setState(83);
			match(RCURLY);
			setState(85);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				{
				setState(84);
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
		enterRule(_localctx, 14, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(87);
			match(LPAREN);
			setState(88);
			match(ID);
			setState(89);
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
		public PredicateContext predicate() {
			return getRuleContext(PredicateContext.class,0);
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
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_item, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(99);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case TILDA:
			case ID:
				{
				setState(92);
				predicate();
				}
				break;
			case LPAREN:
				{
				setState(93);
				match(LPAREN);
				setState(94);
				item(0);
				setState(95);
				match(RPAREN);
				}
				break;
			case EXCL:
				{
				setState(97);
				match(EXCL);
				setState(98);
				item(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(106);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,6,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ItemContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_item);
					setState(101);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(102);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(103);
					item(3);
					}
					} 
				}
				setState(108);
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

	public static class PredicateContext extends ParserRuleContext {
		public LvalContext lval() {
			return getRuleContext(LvalContext.class,0);
		}
		public TerminalNode PRED_OP() { return getToken(NCIntentDslParser.PRED_OP, 0); }
		public RvalContext rval() {
			return getRuleContext(RvalContext.class,0);
		}
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public PredicateContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_predicate; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterPredicate(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitPredicate(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_predicate);
		try {
			setState(120);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(109);
				lval();
				setState(110);
				match(PRED_OP);
				setState(111);
				rval();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(113);
				match(ID);
				setState(114);
				match(LPAREN);
				setState(115);
				lval();
				setState(116);
				match(RPAREN);
				setState(117);
				match(PRED_OP);
				setState(118);
				rval();
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

	public static class LvalContext extends ParserRuleContext {
		public MetaContext meta() {
			return getRuleContext(MetaContext.class,0);
		}
		public LvalQualContext lvalQual() {
			return getRuleContext(LvalQualContext.class,0);
		}
		public LvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLval(this);
		}
	}

	public final LvalContext lval() throws RecognitionException {
		LvalContext _localctx = new LvalContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_lval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(123);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(122);
				lvalQual(0);
				}
			}

			setState(134);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__4:
				{
				setState(125);
				match(T__4);
				}
				break;
			case T__5:
				{
				setState(126);
				match(T__5);
				}
				break;
			case T__6:
				{
				setState(127);
				match(T__6);
				}
				break;
			case T__7:
				{
				setState(128);
				match(T__7);
				}
				break;
			case T__8:
				{
				setState(129);
				match(T__8);
				}
				break;
			case T__9:
				{
				setState(130);
				match(T__9);
				}
				break;
			case T__10:
				{
				setState(131);
				match(T__10);
				}
				break;
			case T__11:
				{
				setState(132);
				match(T__11);
				}
				break;
			case TILDA:
				{
				setState(133);
				meta();
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

	public static class LvalQualContext extends ParserRuleContext {
		public LvalPartContext lvalPart() {
			return getRuleContext(LvalPartContext.class,0);
		}
		public LvalQualContext lvalQual() {
			return getRuleContext(LvalQualContext.class,0);
		}
		public LvalQualContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalQual; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLvalQual(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLvalQual(this);
		}
	}

	public final LvalQualContext lvalQual() throws RecognitionException {
		return lvalQual(0);
	}

	private LvalQualContext lvalQual(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LvalQualContext _localctx = new LvalQualContext(_ctx, _parentState);
		LvalQualContext _prevctx = _localctx;
		int _startState = 22;
		enterRecursionRule(_localctx, 22, RULE_lvalQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(137);
			lvalPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(143);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LvalQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_lvalQual);
					setState(139);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(140);
					lvalPart();
					}
					} 
				}
				setState(145);
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

	public static class LvalPartContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode DOT() { return getToken(NCIntentDslParser.DOT, 0); }
		public LvalPartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lvalPart; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterLvalPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitLvalPart(this);
		}
	}

	public final LvalPartContext lvalPart() throws RecognitionException {
		LvalPartContext _localctx = new LvalPartContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_lvalPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(146);
			match(ID);
			setState(147);
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

	public static class RvalSingleContext extends ParserRuleContext {
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode EXP() { return getToken(NCIntentDslParser.EXP, 0); }
		public TerminalNode MINUS() { return getToken(NCIntentDslParser.MINUS, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public RvalSingleContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rvalSingle; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterRvalSingle(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitRvalSingle(this);
		}
	}

	public final RvalSingleContext rvalSingle() throws RecognitionException {
		RvalSingleContext _localctx = new RvalSingleContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_rvalSingle);
		int _la;
		try {
			setState(160);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__12:
				enterOuterAlt(_localctx, 1);
				{
				setState(149);
				match(T__12);
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(151);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(150);
					match(MINUS);
					}
				}

				setState(156);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
				case 1:
					{
					setState(153);
					match(INT);
					}
					break;
				case 2:
					{
					setState(154);
					match(INT);
					setState(155);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(158);
				match(BOOL);
				}
				break;
			case QSTRING:
				enterOuterAlt(_localctx, 4);
				{
				setState(159);
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

	public static class RvalContext extends ParserRuleContext {
		public RvalSingleContext rvalSingle() {
			return getRuleContext(RvalSingleContext.class,0);
		}
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public RvalListContext rvalList() {
			return getRuleContext(RvalListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public RvalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterRval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitRval(this);
		}
	}

	public final RvalContext rval() throws RecognitionException {
		RvalContext _localctx = new RvalContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_rval);
		try {
			setState(167);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__12:
			case QSTRING:
			case MINUS:
			case BOOL:
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				rvalSingle();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				match(LPAREN);
				setState(164);
				rvalList(0);
				setState(165);
				match(RPAREN);
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

	public static class RvalListContext extends ParserRuleContext {
		public RvalSingleContext rvalSingle() {
			return getRuleContext(RvalSingleContext.class,0);
		}
		public RvalListContext rvalList() {
			return getRuleContext(RvalListContext.class,0);
		}
		public TerminalNode COMMA() { return getToken(NCIntentDslParser.COMMA, 0); }
		public RvalListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rvalList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterRvalList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitRvalList(this);
		}
	}

	public final RvalListContext rvalList() throws RecognitionException {
		return rvalList(0);
	}

	private RvalListContext rvalList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		RvalListContext _localctx = new RvalListContext(_ctx, _parentState);
		RvalListContext _prevctx = _localctx;
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_rvalList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(170);
			rvalSingle();
			}
			_ctx.stop = _input.LT(-1);
			setState(177);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new RvalListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_rvalList);
					setState(172);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(173);
					match(COMMA);
					setState(174);
					rvalSingle();
					}
					} 
				}
				setState(179);
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

	public static class MetaContext extends ParserRuleContext {
		public TerminalNode TILDA() { return getToken(NCIntentDslParser.TILDA, 0); }
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LBR() { return getToken(NCIntentDslParser.LBR, 0); }
		public TerminalNode INT() { return getToken(NCIntentDslParser.INT, 0); }
		public TerminalNode RBR() { return getToken(NCIntentDslParser.RBR, 0); }
		public QstringContext qstring() {
			return getRuleContext(QstringContext.class,0);
		}
		public MetaContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_meta; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterMeta(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitMeta(this);
		}
	}

	public final MetaContext meta() throws RecognitionException {
		MetaContext _localctx = new MetaContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_meta);
		try {
			setState(193);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(180);
				match(TILDA);
				setState(181);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(182);
				match(TILDA);
				setState(183);
				match(ID);
				setState(184);
				match(LBR);
				setState(185);
				match(INT);
				setState(186);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(187);
				match(TILDA);
				setState(188);
				match(ID);
				setState(189);
				match(LBR);
				setState(190);
				qstring();
				setState(191);
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
		public TerminalNode QSTRING() { return getToken(NCIntentDslParser.QSTRING, 0); }
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
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(195);
			match(QSTRING);
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
			setState(199);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(197);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(198);
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
		enterRule(_localctx, 38, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(201);
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
		enterRule(_localctx, 40, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			match(LBR);
			setState(204);
			match(INT);
			setState(205);
			match(COMMA);
			setState(206);
			match(INT);
			setState(207);
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
		case 4:
			return terms_sempred((TermsContext)_localctx, predIndex);
		case 8:
			return item_sempred((ItemContext)_localctx, predIndex);
		case 11:
			return lvalQual_sempred((LvalQualContext)_localctx, predIndex);
		case 15:
			return rvalList_sempred((RvalListContext)_localctx, predIndex);
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
	private boolean item_sempred(ItemContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean lvalQual_sempred(LvalQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean rvalList_sempred(RvalListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3/\u00d4\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\3\2\3\2\5\2/\n\2\3\2\5\2\62\n"+
		"\2\3\2\3\2\3\2\3\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6"+
		"\3\6\3\6\3\6\7\6H\n\6\f\6\16\6K\13\6\3\7\3\7\3\b\3\b\5\bQ\n\b\3\b\3\b"+
		"\3\b\3\b\3\b\5\bX\n\b\3\t\3\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\5\nf\n\n\3\n\3\n\3\n\7\nk\n\n\f\n\16\nn\13\n\3\13\3\13\3\13\3\13\3\13"+
		"\3\13\3\13\3\13\3\13\3\13\3\13\5\13{\n\13\3\f\5\f~\n\f\3\f\3\f\3\f\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\5\f\u0089\n\f\3\r\3\r\3\r\3\r\3\r\7\r\u0090\n\r\f"+
		"\r\16\r\u0093\13\r\3\16\3\16\3\16\3\17\3\17\5\17\u009a\n\17\3\17\3\17"+
		"\3\17\5\17\u009f\n\17\3\17\3\17\5\17\u00a3\n\17\3\20\3\20\3\20\3\20\3"+
		"\20\5\20\u00aa\n\20\3\21\3\21\3\21\3\21\3\21\3\21\7\21\u00b2\n\21\f\21"+
		"\16\21\u00b5\13\21\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3\22\3"+
		"\22\3\22\3\22\5\22\u00c4\n\22\3\23\3\23\3\24\3\24\5\24\u00ca\n\24\3\25"+
		"\3\25\3\26\3\26\3\26\3\26\3\26\3\26\3\26\2\6\n\22\30 \27\2\4\6\b\n\f\16"+
		"\20\22\24\26\30\32\34\36 \"$&(*\2\5\4\2\33\33$$\3\2\22\23\3\2%\'\2\u00db"+
		"\2,\3\2\2\2\4\66\3\2\2\2\6:\3\2\2\2\b>\3\2\2\2\nB\3\2\2\2\fL\3\2\2\2\16"+
		"N\3\2\2\2\20Y\3\2\2\2\22e\3\2\2\2\24z\3\2\2\2\26}\3\2\2\2\30\u008a\3\2"+
		"\2\2\32\u0094\3\2\2\2\34\u00a2\3\2\2\2\36\u00a9\3\2\2\2 \u00ab\3\2\2\2"+
		"\"\u00c3\3\2\2\2$\u00c5\3\2\2\2&\u00c9\3\2\2\2(\u00cb\3\2\2\2*\u00cd\3"+
		"\2\2\2,.\5\4\3\2-/\5\6\4\2.-\3\2\2\2./\3\2\2\2/\61\3\2\2\2\60\62\5\b\5"+
		"\2\61\60\3\2\2\2\61\62\3\2\2\2\62\63\3\2\2\2\63\64\5\n\6\2\64\65\7\2\2"+
		"\3\65\3\3\2\2\2\66\67\7\3\2\2\678\7$\2\289\7-\2\29\5\3\2\2\2:;\7\4\2\2"+
		";<\7$\2\2<=\7*\2\2=\7\3\2\2\2>?\7\5\2\2?@\7$\2\2@A\5$\23\2A\t\3\2\2\2"+
		"BC\b\6\1\2CD\5\16\b\2DI\3\2\2\2EF\f\3\2\2FH\5\16\b\2GE\3\2\2\2HK\3\2\2"+
		"\2IG\3\2\2\2IJ\3\2\2\2J\13\3\2\2\2KI\3\2\2\2LM\t\2\2\2M\r\3\2\2\2NP\7"+
		"\6\2\2OQ\5\20\t\2PO\3\2\2\2PQ\3\2\2\2QR\3\2\2\2RS\5\f\7\2ST\7\30\2\2T"+
		"U\5\22\n\2UW\7\31\2\2VX\5&\24\2WV\3\2\2\2WX\3\2\2\2X\17\3\2\2\2YZ\7\26"+
		"\2\2Z[\7-\2\2[\\\7\27\2\2\\\21\3\2\2\2]^\b\n\1\2^f\5\24\13\2_`\7\26\2"+
		"\2`a\5\22\n\2ab\7\27\2\2bf\3\2\2\2cd\7\25\2\2df\5\22\n\3e]\3\2\2\2e_\3"+
		"\2\2\2ec\3\2\2\2fl\3\2\2\2gh\f\4\2\2hi\t\3\2\2ik\5\22\n\5jg\3\2\2\2kn"+
		"\3\2\2\2lj\3\2\2\2lm\3\2\2\2m\23\3\2\2\2nl\3\2\2\2op\5\26\f\2pq\7\21\2"+
		"\2qr\5\36\20\2r{\3\2\2\2st\7-\2\2tu\7\26\2\2uv\5\26\f\2vw\7\27\2\2wx\7"+
		"\21\2\2xy\5\36\20\2y{\3\2\2\2zo\3\2\2\2zs\3\2\2\2{\25\3\2\2\2|~\5\30\r"+
		"\2}|\3\2\2\2}~\3\2\2\2~\u0088\3\2\2\2\177\u0089\7\7\2\2\u0080\u0089\7"+
		"\b\2\2\u0081\u0089\7\t\2\2\u0082\u0089\7\n\2\2\u0083\u0089\7\13\2\2\u0084"+
		"\u0089\7\f\2\2\u0085\u0089\7\r\2\2\u0086\u0089\7\16\2\2\u0087\u0089\5"+
		"\"\22\2\u0088\177\3\2\2\2\u0088\u0080\3\2\2\2\u0088\u0081\3\2\2\2\u0088"+
		"\u0082\3\2\2\2\u0088\u0083\3\2\2\2\u0088\u0084\3\2\2\2\u0088\u0085\3\2"+
		"\2\2\u0088\u0086\3\2\2\2\u0088\u0087\3\2\2\2\u0089\27\3\2\2\2\u008a\u008b"+
		"\b\r\1\2\u008b\u008c\5\32\16\2\u008c\u0091\3\2\2\2\u008d\u008e\f\3\2\2"+
		"\u008e\u0090\5\32\16\2\u008f\u008d\3\2\2\2\u0090\u0093\3\2\2\2\u0091\u008f"+
		"\3\2\2\2\u0091\u0092\3\2\2\2\u0092\31\3\2\2\2\u0093\u0091\3\2\2\2\u0094"+
		"\u0095\7-\2\2\u0095\u0096\7\"\2\2\u0096\33\3\2\2\2\u0097\u00a3\7\17\2"+
		"\2\u0098\u009a\7!\2\2\u0099\u0098\3\2\2\2\u0099\u009a\3\2\2\2\u009a\u009e"+
		"\3\2\2\2\u009b\u009f\7+\2\2\u009c\u009d\7+\2\2\u009d\u009f\7,\2\2\u009e"+
		"\u009b\3\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a3\3\2\2\2\u00a0\u00a3\7*"+
		"\2\2\u00a1\u00a3\5$\23\2\u00a2\u0097\3\2\2\2\u00a2\u0099\3\2\2\2\u00a2"+
		"\u00a0\3\2\2\2\u00a2\u00a1\3\2\2\2\u00a3\35\3\2\2\2\u00a4\u00aa\5\34\17"+
		"\2\u00a5\u00a6\7\26\2\2\u00a6\u00a7\5 \21\2\u00a7\u00a8\7\27\2\2\u00a8"+
		"\u00aa\3\2\2\2\u00a9\u00a4\3\2\2\2\u00a9\u00a5\3\2\2\2\u00aa\37\3\2\2"+
		"\2\u00ab\u00ac\b\21\1\2\u00ac\u00ad\5\34\17\2\u00ad\u00b3\3\2\2\2\u00ae"+
		"\u00af\f\3\2\2\u00af\u00b0\7\37\2\2\u00b0\u00b2\5\34\17\2\u00b1\u00ae"+
		"\3\2\2\2\u00b2\u00b5\3\2\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b4\3\2\2\2\u00b4"+
		"!\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b6\u00b7\7\33\2\2\u00b7\u00c4\7-\2\2"+
		"\u00b8\u00b9\7\33\2\2\u00b9\u00ba\7-\2\2\u00ba\u00bb\7\35\2\2\u00bb\u00bc"+
		"\7+\2\2\u00bc\u00c4\7\36\2\2\u00bd\u00be\7\33\2\2\u00be\u00bf\7-\2\2\u00bf"+
		"\u00c0\7\35\2\2\u00c0\u00c1\5$\23\2\u00c1\u00c2\7\36\2\2\u00c2\u00c4\3"+
		"\2\2\2\u00c3\u00b6\3\2\2\2\u00c3\u00b8\3\2\2\2\u00c3\u00bd\3\2\2\2\u00c4"+
		"#\3\2\2\2\u00c5\u00c6\7\20\2\2\u00c6%\3\2\2\2\u00c7\u00ca\5(\25\2\u00c8"+
		"\u00ca\5*\26\2\u00c9\u00c7\3\2\2\2\u00c9\u00c8\3\2\2\2\u00ca\'\3\2\2\2"+
		"\u00cb\u00cc\t\4\2\2\u00cc)\3\2\2\2\u00cd\u00ce\7\35\2\2\u00ce\u00cf\7"+
		"+\2\2\u00cf\u00d0\7\37\2\2\u00d0\u00d1\7+\2\2\u00d1\u00d2\7\36\2\2\u00d2"+
		"+\3\2\2\2\24.\61IPWelz}\u0088\u0091\u0099\u009e\u00a2\u00a9\u00b3\u00c3"+
		"\u00c9";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}