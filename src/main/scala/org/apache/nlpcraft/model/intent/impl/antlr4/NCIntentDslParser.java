// Generated from /Users/xxx/nlpcraft/src/main/scala/org/apache/nlpcraft/model/intent/impl/antlr4/NCIntentDsl.g4 by ANTLR 4.8
package org.apache.nlpcraft.model.intent.impl.antlr4;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class NCIntentDslParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, PRED_OP=15, AND=16, OR=17, 
		VERT=18, EXCL=19, LPAREN=20, RPAREN=21, LCURLY=22, RCURLY=23, SQUOTE=24, 
		TILDA=25, RIGHT=26, LBR=27, RBR=28, COMMA=29, COLON=30, MINUS=31, DOT=32, 
		UNDERSCORE=33, EQ=34, PLUS=35, QUESTION=36, STAR=37, BOOL=38, INT=39, 
		EXP=40, ID=41, WS=42, ErrorCharacter=43;
	public static final int
		RULE_intent = 0, RULE_intentId = 1, RULE_convDecl = 2, RULE_orderedDecl = 3, 
		RULE_flowDecl = 4, RULE_flow = 5, RULE_flowItem = 6, RULE_flowItemIds = 7, 
		RULE_idList = 8, RULE_terms = 9, RULE_term = 10, RULE_termId = 11, RULE_item = 12, 
		RULE_predicate = 13, RULE_lval = 14, RULE_lvalQual = 15, RULE_lvalPart = 16, 
		RULE_rvalSingle = 17, RULE_rval = 18, RULE_rvalList = 19, RULE_meta = 20, 
		RULE_qstring = 21, RULE_minMax = 22, RULE_minMaxShortcut = 23, RULE_minMaxRange = 24;
	private static String[] makeRuleNames() {
		return new String[] {
			"intent", "intentId", "convDecl", "orderedDecl", "flowDecl", "flow", 
			"flowItem", "flowItemIds", "idList", "terms", "term", "termId", "item", 
			"predicate", "lval", "lvalQual", "lvalPart", "rvalSingle", "rval", "rvalList", 
			"meta", "qstring", "minMax", "minMaxShortcut", "minMaxRange"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'intent'", "'conv'", "'ordered'", "'flow'", "'term'", "'id'", 
			"'aliases'", "'startidx'", "'endidx'", "'parent'", "'groups'", "'ancestors'", 
			"'value'", "'null'", null, "'&&'", "'||'", "'|'", "'!'", "'('", "')'", 
			"'{'", "'}'", "'''", "'~'", "'>>'", "'['", "']'", "','", "':'", "'-'", 
			"'.'", "'_'", "'='", "'+'", "'?'", "'*'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, "PRED_OP", "AND", "OR", "VERT", "EXCL", "LPAREN", "RPAREN", 
			"LCURLY", "RCURLY", "SQUOTE", "TILDA", "RIGHT", "LBR", "RBR", "COMMA", 
			"COLON", "MINUS", "DOT", "UNDERSCORE", "EQ", "PLUS", "QUESTION", "STAR", 
			"BOOL", "INT", "EXP", "ID", "WS", "ErrorCharacter"
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
		public ConvDeclContext convDecl() {
			return getRuleContext(ConvDeclContext.class,0);
		}
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitIntent(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntentContext intent() throws RecognitionException {
		IntentContext _localctx = new IntentContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_intent);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			intentId();
			setState(52);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(51);
				convDecl();
				}
			}

			setState(55);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(54);
				orderedDecl();
				}
			}

			setState(58);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__3) {
				{
				setState(57);
				flowDecl();
				}
			}

			setState(60);
			terms(0);
			setState(61);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitIntentId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IntentIdContext intentId() throws RecognitionException {
		IntentIdContext _localctx = new IntentIdContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_intentId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(63);
			match(T__0);
			setState(64);
			match(EQ);
			setState(65);
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

	public static class ConvDeclContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
		public TerminalNode BOOL() { return getToken(NCIntentDslParser.BOOL, 0); }
		public ConvDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_convDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterConvDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitConvDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitConvDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ConvDeclContext convDecl() throws RecognitionException {
		ConvDeclContext _localctx = new ConvDeclContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_convDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			match(T__1);
			setState(68);
			match(EQ);
			setState(69);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitOrderedDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final OrderedDeclContext orderedDecl() throws RecognitionException {
		OrderedDeclContext _localctx = new OrderedDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_orderedDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(71);
			match(T__2);
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
		public List<TerminalNode> SQUOTE() { return getTokens(NCIntentDslParser.SQUOTE); }
		public TerminalNode SQUOTE(int i) {
			return getToken(NCIntentDslParser.SQUOTE, i);
		}
		public FlowContext flow() {
			return getRuleContext(FlowContext.class,0);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitFlowDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlowDeclContext flowDecl() throws RecognitionException {
		FlowDeclContext _localctx = new FlowDeclContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_flowDecl);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			match(T__3);
			setState(76);
			match(EQ);
			setState(77);
			match(SQUOTE);
			setState(78);
			flow(0);
			setState(79);
			match(SQUOTE);
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

	public static class FlowContext extends ParserRuleContext {
		public FlowItemContext flowItem() {
			return getRuleContext(FlowItemContext.class,0);
		}
		public FlowContext flow() {
			return getRuleContext(FlowContext.class,0);
		}
		public TerminalNode RIGHT() { return getToken(NCIntentDslParser.RIGHT, 0); }
		public FlowContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flow; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlow(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlow(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitFlow(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlowContext flow() throws RecognitionException {
		return flow(0);
	}

	private FlowContext flow(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		FlowContext _localctx = new FlowContext(_ctx, _parentState);
		FlowContext _prevctx = _localctx;
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_flow, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(83);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
			case 1:
				{
				}
				break;
			case 2:
				{
				setState(82);
				flowItem();
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(90);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new FlowContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_flow);
					setState(85);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(86);
					match(RIGHT);
					setState(87);
					flowItem();
					}
					} 
				}
				setState(92);
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

	public static class FlowItemContext extends ParserRuleContext {
		public FlowItemIdsContext flowItemIds() {
			return getRuleContext(FlowItemIdsContext.class,0);
		}
		public MinMaxContext minMax() {
			return getRuleContext(MinMaxContext.class,0);
		}
		public FlowItemContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowItem; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlowItem(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlowItem(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitFlowItem(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlowItemContext flowItem() throws RecognitionException {
		FlowItemContext _localctx = new FlowItemContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_flowItem);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(93);
			flowItemIds();
			setState(95);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				{
				setState(94);
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

	public static class FlowItemIdsContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public TerminalNode LPAREN() { return getToken(NCIntentDslParser.LPAREN, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TerminalNode RPAREN() { return getToken(NCIntentDslParser.RPAREN, 0); }
		public FlowItemIdsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_flowItemIds; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterFlowItemIds(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitFlowItemIds(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitFlowItemIds(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FlowItemIdsContext flowItemIds() throws RecognitionException {
		FlowItemIdsContext _localctx = new FlowItemIdsContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_flowItemIds);
		try {
			setState(102);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				enterOuterAlt(_localctx, 1);
				{
				setState(97);
				match(ID);
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(98);
				match(LPAREN);
				setState(99);
				idList(0);
				setState(100);
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

	public static class IdListContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(NCIntentDslParser.ID, 0); }
		public IdListContext idList() {
			return getRuleContext(IdListContext.class,0);
		}
		public TerminalNode VERT() { return getToken(NCIntentDslParser.VERT, 0); }
		public IdListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_idList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).enterIdList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof NCIntentDslListener ) ((NCIntentDslListener)listener).exitIdList(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitIdList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final IdListContext idList() throws RecognitionException {
		return idList(0);
	}

	private IdListContext idList(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		IdListContext _localctx = new IdListContext(_ctx, _parentState);
		IdListContext _prevctx = _localctx;
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_idList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(105);
			match(ID);
			}
			_ctx.stop = _input.LT(-1);
			setState(112);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new IdListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_idList);
					setState(107);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(108);
					match(VERT);
					setState(109);
					match(ID);
					}
					} 
				}
				setState(114);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitTerms(this);
			else return visitor.visitChildren(this);
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
			setState(116);
			term();
			}
			_ctx.stop = _input.LT(-1);
			setState(122);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new TermsContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_terms);
					setState(118);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(119);
					term();
					}
					} 
				}
				setState(124);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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

	public static class TermContext extends ParserRuleContext {
		public TerminalNode EQ() { return getToken(NCIntentDslParser.EQ, 0); }
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitTerm(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_term);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			match(T__4);
			setState(127);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==LPAREN) {
				{
				setState(126);
				termId();
				}
			}

			setState(129);
			match(EQ);
			setState(130);
			match(LCURLY);
			setState(131);
			item(0);
			setState(132);
			match(RCURLY);
			setState(134);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				setState(133);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitTermId(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TermIdContext termId() throws RecognitionException {
		TermIdContext _localctx = new TermIdContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_termId);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(136);
			match(LPAREN);
			setState(137);
			match(ID);
			setState(138);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitItem(this);
			else return visitor.visitChildren(this);
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
			setState(148);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
			case T__6:
			case T__7:
			case T__8:
			case T__9:
			case T__10:
			case T__11:
			case T__12:
			case TILDA:
			case ID:
				{
				setState(141);
				predicate();
				}
				break;
			case LPAREN:
				{
				setState(142);
				match(LPAREN);
				setState(143);
				item(0);
				setState(144);
				match(RPAREN);
				}
				break;
			case EXCL:
				{
				setState(146);
				match(EXCL);
				setState(147);
				item(1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(155);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new ItemContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_item);
					setState(150);
					if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
					setState(151);
					_la = _input.LA(1);
					if ( !(_la==AND || _la==OR) ) {
					_errHandler.recoverInline(this);
					}
					else {
						if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
						_errHandler.reportMatch(this);
						consume();
					}
					setState(152);
					item(3);
					}
					} 
				}
				setState(157);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitPredicate(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PredicateContext predicate() throws RecognitionException {
		PredicateContext _localctx = new PredicateContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_predicate);
		try {
			setState(169);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,13,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(158);
				lval();
				setState(159);
				match(PRED_OP);
				setState(160);
				rval();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(162);
				match(ID);
				setState(163);
				match(LPAREN);
				setState(164);
				lval();
				setState(165);
				match(RPAREN);
				setState(166);
				match(PRED_OP);
				setState(167);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitLval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalContext lval() throws RecognitionException {
		LvalContext _localctx = new LvalContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_lval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==ID) {
				{
				setState(171);
				lvalQual(0);
				}
			}

			setState(183);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__5:
				{
				setState(174);
				match(T__5);
				}
				break;
			case T__6:
				{
				setState(175);
				match(T__6);
				}
				break;
			case T__7:
				{
				setState(176);
				match(T__7);
				}
				break;
			case T__8:
				{
				setState(177);
				match(T__8);
				}
				break;
			case T__9:
				{
				setState(178);
				match(T__9);
				}
				break;
			case T__10:
				{
				setState(179);
				match(T__10);
				}
				break;
			case T__11:
				{
				setState(180);
				match(T__11);
				}
				break;
			case T__12:
				{
				setState(181);
				match(T__12);
				}
				break;
			case TILDA:
				{
				setState(182);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitLvalQual(this);
			else return visitor.visitChildren(this);
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
		int _startState = 30;
		enterRecursionRule(_localctx, 30, RULE_lvalQual, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(186);
			lvalPart();
			}
			_ctx.stop = _input.LT(-1);
			setState(192);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new LvalQualContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_lvalQual);
					setState(188);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(189);
					lvalPart();
					}
					} 
				}
				setState(194);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitLvalPart(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LvalPartContext lvalPart() throws RecognitionException {
		LvalPartContext _localctx = new LvalPartContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_lvalPart);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(195);
			match(ID);
			setState(196);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitRvalSingle(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RvalSingleContext rvalSingle() throws RecognitionException {
		RvalSingleContext _localctx = new RvalSingleContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_rvalSingle);
		int _la;
		try {
			setState(209);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__13:
				enterOuterAlt(_localctx, 1);
				{
				setState(198);
				match(T__13);
				}
				break;
			case MINUS:
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(200);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==MINUS) {
					{
					setState(199);
					match(MINUS);
					}
				}

				setState(205);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,18,_ctx) ) {
				case 1:
					{
					setState(202);
					match(INT);
					}
					break;
				case 2:
					{
					setState(203);
					match(INT);
					setState(204);
					match(EXP);
					}
					break;
				}
				}
				break;
			case BOOL:
				enterOuterAlt(_localctx, 3);
				{
				setState(207);
				match(BOOL);
				}
				break;
			case SQUOTE:
				enterOuterAlt(_localctx, 4);
				{
				setState(208);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitRval(this);
			else return visitor.visitChildren(this);
		}
	}

	public final RvalContext rval() throws RecognitionException {
		RvalContext _localctx = new RvalContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_rval);
		try {
			setState(216);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__13:
			case SQUOTE:
			case MINUS:
			case BOOL:
			case INT:
				enterOuterAlt(_localctx, 1);
				{
				setState(211);
				rvalSingle();
				}
				break;
			case LPAREN:
				enterOuterAlt(_localctx, 2);
				{
				setState(212);
				match(LPAREN);
				setState(213);
				rvalList(0);
				setState(214);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitRvalList(this);
			else return visitor.visitChildren(this);
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
		int _startState = 38;
		enterRecursionRule(_localctx, 38, RULE_rvalList, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			{
			setState(219);
			rvalSingle();
			}
			_ctx.stop = _input.LT(-1);
			setState(226);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					{
					_localctx = new RvalListContext(_parentctx, _parentState);
					pushNewRecursionContext(_localctx, _startState, RULE_rvalList);
					setState(221);
					if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
					setState(222);
					match(COMMA);
					setState(223);
					rvalSingle();
					}
					} 
				}
				setState(228);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitMeta(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MetaContext meta() throws RecognitionException {
		MetaContext _localctx = new MetaContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_meta);
		try {
			setState(242);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,22,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(229);
				match(TILDA);
				setState(230);
				match(ID);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(231);
				match(TILDA);
				setState(232);
				match(ID);
				setState(233);
				match(LBR);
				setState(234);
				match(INT);
				setState(235);
				match(RBR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(236);
				match(TILDA);
				setState(237);
				match(ID);
				setState(238);
				match(LBR);
				setState(239);
				qstring();
				setState(240);
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
		public List<TerminalNode> SQUOTE() { return getTokens(NCIntentDslParser.SQUOTE); }
		public TerminalNode SQUOTE(int i) {
			return getToken(NCIntentDslParser.SQUOTE, i);
		}
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitQstring(this);
			else return visitor.visitChildren(this);
		}
	}

	public final QstringContext qstring() throws RecognitionException {
		QstringContext _localctx = new QstringContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_qstring);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(244);
			match(SQUOTE);
			setState(248);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << T__13) | (1L << PRED_OP) | (1L << AND) | (1L << OR) | (1L << VERT) | (1L << EXCL) | (1L << LPAREN) | (1L << RPAREN) | (1L << LCURLY) | (1L << RCURLY) | (1L << TILDA) | (1L << RIGHT) | (1L << LBR) | (1L << RBR) | (1L << COMMA) | (1L << COLON) | (1L << MINUS) | (1L << DOT) | (1L << UNDERSCORE) | (1L << EQ) | (1L << PLUS) | (1L << QUESTION) | (1L << STAR) | (1L << BOOL) | (1L << INT) | (1L << EXP) | (1L << ID) | (1L << WS) | (1L << ErrorCharacter))) != 0)) {
				{
				{
				setState(245);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==SQUOTE) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(250);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(251);
			match(SQUOTE);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitMinMax(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MinMaxContext minMax() throws RecognitionException {
		MinMaxContext _localctx = new MinMaxContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_minMax);
		try {
			setState(255);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case PLUS:
			case QUESTION:
			case STAR:
				enterOuterAlt(_localctx, 1);
				{
				setState(253);
				minMaxShortcut();
				}
				break;
			case LBR:
				enterOuterAlt(_localctx, 2);
				{
				setState(254);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitMinMaxShortcut(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MinMaxShortcutContext minMaxShortcut() throws RecognitionException {
		MinMaxShortcutContext _localctx = new MinMaxShortcutContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_minMaxShortcut);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(257);
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
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof NCIntentDslVisitor ) return ((NCIntentDslVisitor<? extends T>)visitor).visitMinMaxRange(this);
			else return visitor.visitChildren(this);
		}
	}

	public final MinMaxRangeContext minMaxRange() throws RecognitionException {
		MinMaxRangeContext _localctx = new MinMaxRangeContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_minMaxRange);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(259);
			match(LBR);
			setState(260);
			match(INT);
			setState(261);
			match(COMMA);
			setState(262);
			match(INT);
			setState(263);
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
			return flow_sempred((FlowContext)_localctx, predIndex);
		case 8:
			return idList_sempred((IdListContext)_localctx, predIndex);
		case 9:
			return terms_sempred((TermsContext)_localctx, predIndex);
		case 12:
			return item_sempred((ItemContext)_localctx, predIndex);
		case 15:
			return lvalQual_sempred((LvalQualContext)_localctx, predIndex);
		case 19:
			return rvalList_sempred((RvalListContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean flow_sempred(FlowContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean idList_sempred(IdListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 1:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean terms_sempred(TermsContext _localctx, int predIndex) {
		switch (predIndex) {
		case 2:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean item_sempred(ItemContext _localctx, int predIndex) {
		switch (predIndex) {
		case 3:
			return precpred(_ctx, 2);
		}
		return true;
	}
	private boolean lvalQual_sempred(LvalQualContext _localctx, int predIndex) {
		switch (predIndex) {
		case 4:
			return precpred(_ctx, 1);
		}
		return true;
	}
	private boolean rvalList_sempred(RvalListContext _localctx, int predIndex) {
		switch (predIndex) {
		case 5:
			return precpred(_ctx, 1);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3-\u010c\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\3\2\3\2\5\2\67\n\2\3\2\5\2:\n\2\3\2\5\2=\n\2\3\2\3\2\3\2\3"+
		"\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\6"+
		"\3\7\3\7\5\7V\n\7\3\7\3\7\3\7\7\7[\n\7\f\7\16\7^\13\7\3\b\3\b\5\bb\n\b"+
		"\3\t\3\t\3\t\3\t\3\t\5\ti\n\t\3\n\3\n\3\n\3\n\3\n\3\n\7\nq\n\n\f\n\16"+
		"\nt\13\n\3\13\3\13\3\13\3\13\3\13\7\13{\n\13\f\13\16\13~\13\13\3\f\3\f"+
		"\5\f\u0082\n\f\3\f\3\f\3\f\3\f\3\f\5\f\u0089\n\f\3\r\3\r\3\r\3\r\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\5\16\u0097\n\16\3\16\3\16\3\16\7\16"+
		"\u009c\n\16\f\16\16\16\u009f\13\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\17\3\17\3\17\3\17\5\17\u00ac\n\17\3\20\5\20\u00af\n\20\3\20\3\20\3"+
		"\20\3\20\3\20\3\20\3\20\3\20\3\20\5\20\u00ba\n\20\3\21\3\21\3\21\3\21"+
		"\3\21\7\21\u00c1\n\21\f\21\16\21\u00c4\13\21\3\22\3\22\3\22\3\23\3\23"+
		"\5\23\u00cb\n\23\3\23\3\23\3\23\5\23\u00d0\n\23\3\23\3\23\5\23\u00d4\n"+
		"\23\3\24\3\24\3\24\3\24\3\24\5\24\u00db\n\24\3\25\3\25\3\25\3\25\3\25"+
		"\3\25\7\25\u00e3\n\25\f\25\16\25\u00e6\13\25\3\26\3\26\3\26\3\26\3\26"+
		"\3\26\3\26\3\26\3\26\3\26\3\26\3\26\3\26\5\26\u00f5\n\26\3\27\3\27\7\27"+
		"\u00f9\n\27\f\27\16\27\u00fc\13\27\3\27\3\27\3\30\3\30\5\30\u0102\n\30"+
		"\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\32\3\32\2\b\f\22\24\32 (\33\2\4"+
		"\6\b\n\f\16\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\2\5\3\2\22\23\3\2\32"+
		"\32\3\2%\'\2\u0116\2\64\3\2\2\2\4A\3\2\2\2\6E\3\2\2\2\bI\3\2\2\2\nM\3"+
		"\2\2\2\fU\3\2\2\2\16_\3\2\2\2\20h\3\2\2\2\22j\3\2\2\2\24u\3\2\2\2\26\177"+
		"\3\2\2\2\30\u008a\3\2\2\2\32\u0096\3\2\2\2\34\u00ab\3\2\2\2\36\u00ae\3"+
		"\2\2\2 \u00bb\3\2\2\2\"\u00c5\3\2\2\2$\u00d3\3\2\2\2&\u00da\3\2\2\2(\u00dc"+
		"\3\2\2\2*\u00f4\3\2\2\2,\u00f6\3\2\2\2.\u0101\3\2\2\2\60\u0103\3\2\2\2"+
		"\62\u0105\3\2\2\2\64\66\5\4\3\2\65\67\5\6\4\2\66\65\3\2\2\2\66\67\3\2"+
		"\2\2\679\3\2\2\28:\5\b\5\298\3\2\2\29:\3\2\2\2:<\3\2\2\2;=\5\n\6\2<;\3"+
		"\2\2\2<=\3\2\2\2=>\3\2\2\2>?\5\24\13\2?@\7\2\2\3@\3\3\2\2\2AB\7\3\2\2"+
		"BC\7$\2\2CD\7+\2\2D\5\3\2\2\2EF\7\4\2\2FG\7$\2\2GH\7(\2\2H\7\3\2\2\2I"+
		"J\7\5\2\2JK\7$\2\2KL\7(\2\2L\t\3\2\2\2MN\7\6\2\2NO\7$\2\2OP\7\32\2\2P"+
		"Q\5\f\7\2QR\7\32\2\2R\13\3\2\2\2SV\b\7\1\2TV\5\16\b\2US\3\2\2\2UT\3\2"+
		"\2\2V\\\3\2\2\2WX\f\3\2\2XY\7\34\2\2Y[\5\16\b\2ZW\3\2\2\2[^\3\2\2\2\\"+
		"Z\3\2\2\2\\]\3\2\2\2]\r\3\2\2\2^\\\3\2\2\2_a\5\20\t\2`b\5.\30\2a`\3\2"+
		"\2\2ab\3\2\2\2b\17\3\2\2\2ci\7+\2\2de\7\26\2\2ef\5\22\n\2fg\7\27\2\2g"+
		"i\3\2\2\2hc\3\2\2\2hd\3\2\2\2i\21\3\2\2\2jk\b\n\1\2kl\7+\2\2lr\3\2\2\2"+
		"mn\f\3\2\2no\7\24\2\2oq\7+\2\2pm\3\2\2\2qt\3\2\2\2rp\3\2\2\2rs\3\2\2\2"+
		"s\23\3\2\2\2tr\3\2\2\2uv\b\13\1\2vw\5\26\f\2w|\3\2\2\2xy\f\3\2\2y{\5\26"+
		"\f\2zx\3\2\2\2{~\3\2\2\2|z\3\2\2\2|}\3\2\2\2}\25\3\2\2\2~|\3\2\2\2\177"+
		"\u0081\7\7\2\2\u0080\u0082\5\30\r\2\u0081\u0080\3\2\2\2\u0081\u0082\3"+
		"\2\2\2\u0082\u0083\3\2\2\2\u0083\u0084\7$\2\2\u0084\u0085\7\30\2\2\u0085"+
		"\u0086\5\32\16\2\u0086\u0088\7\31\2\2\u0087\u0089\5.\30\2\u0088\u0087"+
		"\3\2\2\2\u0088\u0089\3\2\2\2\u0089\27\3\2\2\2\u008a\u008b\7\26\2\2\u008b"+
		"\u008c\7+\2\2\u008c\u008d\7\27\2\2\u008d\31\3\2\2\2\u008e\u008f\b\16\1"+
		"\2\u008f\u0097\5\34\17\2\u0090\u0091\7\26\2\2\u0091\u0092\5\32\16\2\u0092"+
		"\u0093\7\27\2\2\u0093\u0097\3\2\2\2\u0094\u0095\7\25\2\2\u0095\u0097\5"+
		"\32\16\3\u0096\u008e\3\2\2\2\u0096\u0090\3\2\2\2\u0096\u0094\3\2\2\2\u0097"+
		"\u009d\3\2\2\2\u0098\u0099\f\4\2\2\u0099\u009a\t\2\2\2\u009a\u009c\5\32"+
		"\16\5\u009b\u0098\3\2\2\2\u009c\u009f\3\2\2\2\u009d\u009b\3\2\2\2\u009d"+
		"\u009e\3\2\2\2\u009e\33\3\2\2\2\u009f\u009d\3\2\2\2\u00a0\u00a1\5\36\20"+
		"\2\u00a1\u00a2\7\21\2\2\u00a2\u00a3\5&\24\2\u00a3\u00ac\3\2\2\2\u00a4"+
		"\u00a5\7+\2\2\u00a5\u00a6\7\26\2\2\u00a6\u00a7\5\36\20\2\u00a7\u00a8\7"+
		"\27\2\2\u00a8\u00a9\7\21\2\2\u00a9\u00aa\5&\24\2\u00aa\u00ac\3\2\2\2\u00ab"+
		"\u00a0\3\2\2\2\u00ab\u00a4\3\2\2\2\u00ac\35\3\2\2\2\u00ad\u00af\5 \21"+
		"\2\u00ae\u00ad\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b9\3\2\2\2\u00b0\u00ba"+
		"\7\b\2\2\u00b1\u00ba\7\t\2\2\u00b2\u00ba\7\n\2\2\u00b3\u00ba\7\13\2\2"+
		"\u00b4\u00ba\7\f\2\2\u00b5\u00ba\7\r\2\2\u00b6\u00ba\7\16\2\2\u00b7\u00ba"+
		"\7\17\2\2\u00b8\u00ba\5*\26\2\u00b9\u00b0\3\2\2\2\u00b9\u00b1\3\2\2\2"+
		"\u00b9\u00b2\3\2\2\2\u00b9\u00b3\3\2\2\2\u00b9\u00b4\3\2\2\2\u00b9\u00b5"+
		"\3\2\2\2\u00b9\u00b6\3\2\2\2\u00b9\u00b7\3\2\2\2\u00b9\u00b8\3\2\2\2\u00ba"+
		"\37\3\2\2\2\u00bb\u00bc\b\21\1\2\u00bc\u00bd\5\"\22\2\u00bd\u00c2\3\2"+
		"\2\2\u00be\u00bf\f\3\2\2\u00bf\u00c1\5\"\22\2\u00c0\u00be\3\2\2\2\u00c1"+
		"\u00c4\3\2\2\2\u00c2\u00c0\3\2\2\2\u00c2\u00c3\3\2\2\2\u00c3!\3\2\2\2"+
		"\u00c4\u00c2\3\2\2\2\u00c5\u00c6\7+\2\2\u00c6\u00c7\7\"\2\2\u00c7#\3\2"+
		"\2\2\u00c8\u00d4\7\20\2\2\u00c9\u00cb\7!\2\2\u00ca\u00c9\3\2\2\2\u00ca"+
		"\u00cb\3\2\2\2\u00cb\u00cf\3\2\2\2\u00cc\u00d0\7)\2\2\u00cd\u00ce\7)\2"+
		"\2\u00ce\u00d0\7*\2\2\u00cf\u00cc\3\2\2\2\u00cf\u00cd\3\2\2\2\u00d0\u00d4"+
		"\3\2\2\2\u00d1\u00d4\7(\2\2\u00d2\u00d4\5,\27\2\u00d3\u00c8\3\2\2\2\u00d3"+
		"\u00ca\3\2\2\2\u00d3\u00d1\3\2\2\2\u00d3\u00d2\3\2\2\2\u00d4%\3\2\2\2"+
		"\u00d5\u00db\5$\23\2\u00d6\u00d7\7\26\2\2\u00d7\u00d8\5(\25\2\u00d8\u00d9"+
		"\7\27\2\2\u00d9\u00db\3\2\2\2\u00da\u00d5\3\2\2\2\u00da\u00d6\3\2\2\2"+
		"\u00db\'\3\2\2\2\u00dc\u00dd\b\25\1\2\u00dd\u00de\5$\23\2\u00de\u00e4"+
		"\3\2\2\2\u00df\u00e0\f\3\2\2\u00e0\u00e1\7\37\2\2\u00e1\u00e3\5$\23\2"+
		"\u00e2\u00df\3\2\2\2\u00e3\u00e6\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e4\u00e5"+
		"\3\2\2\2\u00e5)\3\2\2\2\u00e6\u00e4\3\2\2\2\u00e7\u00e8\7\33\2\2\u00e8"+
		"\u00f5\7+\2\2\u00e9\u00ea\7\33\2\2\u00ea\u00eb\7+\2\2\u00eb\u00ec\7\35"+
		"\2\2\u00ec\u00ed\7)\2\2\u00ed\u00f5\7\36\2\2\u00ee\u00ef\7\33\2\2\u00ef"+
		"\u00f0\7+\2\2\u00f0\u00f1\7\35\2\2\u00f1\u00f2\5,\27\2\u00f2\u00f3\7\36"+
		"\2\2\u00f3\u00f5\3\2\2\2\u00f4\u00e7\3\2\2\2\u00f4\u00e9\3\2\2\2\u00f4"+
		"\u00ee\3\2\2\2\u00f5+\3\2\2\2\u00f6\u00fa\7\32\2\2\u00f7\u00f9\n\3\2\2"+
		"\u00f8\u00f7\3\2\2\2\u00f9\u00fc\3\2\2\2\u00fa\u00f8\3\2\2\2\u00fa\u00fb"+
		"\3\2\2\2\u00fb\u00fd\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd\u00fe\7\32\2\2"+
		"\u00fe-\3\2\2\2\u00ff\u0102\5\60\31\2\u0100\u0102\5\62\32\2\u0101\u00ff"+
		"\3\2\2\2\u0101\u0100\3\2\2\2\u0102/\3\2\2\2\u0103\u0104\t\4\2\2\u0104"+
		"\61\3\2\2\2\u0105\u0106\7\35\2\2\u0106\u0107\7)\2\2\u0107\u0108\7\37\2"+
		"\2\u0108\u0109\7)\2\2\u0109\u010a\7\36\2\2\u010a\63\3\2\2\2\33\669<U\\"+
		"ahr|\u0081\u0088\u0096\u009d\u00ab\u00ae\u00b9\u00c2\u00ca\u00cf\u00d3"+
		"\u00da\u00e4\u00f4\u00fa\u0101";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}