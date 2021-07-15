package mage.cards.w;

import mage.abilities.Ability;
import mage.abilities.common.BecomesClassLevelTriggeredAbility;
import mage.abilities.common.BeginningOfEndStepTriggeredAbility;
import mage.abilities.condition.common.MorbidCondition;
import mage.abilities.decorator.ConditionalInterveningIfTriggeredAbility;
import mage.abilities.dynamicvalue.common.StaticValue;
import mage.abilities.effects.OneShotEffect;
import mage.abilities.effects.common.LookLibraryAndPickControllerEffect;
import mage.abilities.effects.common.LoseLifeOpponentsEffect;
import mage.abilities.hint.common.MorbidHint;
import mage.abilities.keyword.ClassLevelAbility;
import mage.abilities.keyword.ClassReminderAbility;
import mage.cards.CardImpl;
import mage.cards.CardSetInfo;
import mage.constants.*;
import mage.filter.StaticFilters;
import mage.game.Game;
import mage.players.Player;
import mage.watchers.common.MorbidWatcher;
import mage.watchers.common.PlayerLostLifeWatcher;

import java.util.UUID;

/**
 * @author TheElk801
 */
public final class WarlockClass extends CardImpl {

    public WarlockClass(UUID ownerId, CardSetInfo setInfo) {
        super(ownerId, setInfo, new CardType[]{CardType.ENCHANTMENT}, "{B}");

        this.subtype.add(SubType.CLASS);

        // (Gain the next level as a sorcery to add its ability.)
        this.addAbility(new ClassReminderAbility());

        // At the beginning of your end step, if a creature died this turn, each opponent loses 1 life.
        this.addAbility(new ConditionalInterveningIfTriggeredAbility(
                new BeginningOfEndStepTriggeredAbility(
                        new LoseLifeOpponentsEffect(2), TargetController.YOU, false
                ), MorbidCondition.instance, "At the beginning of your end step, " +
                "if a creature died this turn, each opponent loses 1 life."
        ).addHint(MorbidHint.instance), new MorbidWatcher());

        // {1}{B}: Level 2
        this.addAbility(new ClassLevelAbility(2, "{1}{B}"));

        // When this Class becomes level 2, look at the top three cards of your library. Put one of them into your hand and the rest into your graveyard.
        this.addAbility(new BecomesClassLevelTriggeredAbility(new LookLibraryAndPickControllerEffect(
                StaticValue.get(1), false, StaticValue.get(1), StaticFilters.FILTER_CARD,
                Zone.GRAVEYARD, false, false, false, Zone.HAND, false
        ), 2));

        // {6}{B}: Level 3
        this.addAbility(new ClassLevelAbility(3, "{6}{B}"));

        // At the beginning of your end step, each opponent loses life equal to the life they lost this turn.
        this.addAbility(new BeginningOfEndStepTriggeredAbility(
                new WarlockClassEffect(), TargetController.YOU, false
        ));
    }

    private WarlockClass(final WarlockClass card) {
        super(card);
    }

    @Override
    public WarlockClass copy() {
        return new WarlockClass(this);
    }
}

class WarlockClassEffect extends OneShotEffect {

    WarlockClassEffect() {
        super(Outcome.Benefit);
        staticText = "At the beginning of your end step, each opponent loses life equal to the life they lost this turn.";
    }

    private WarlockClassEffect(final WarlockClassEffect effect) {
        super(effect);
    }

    @Override
    public WarlockClassEffect copy() {
        return new WarlockClassEffect(this);
    }

    @Override
    public boolean apply(Game game, Ability source) {
        PlayerLostLifeWatcher watcher = game.getState().getWatcher(PlayerLostLifeWatcher.class);
        if (watcher == null) {
            return false;
        }
        for (UUID playerId : game.getOpponents(source.getControllerId())) {
            Player opponent = game.getPlayer(playerId);
            if (opponent == null) {
                continue;
            }
            int lifeLost = watcher.getLifeLost(playerId);
            if (lifeLost > 0) {
                opponent.loseLife(lifeLost, game, source, false);
            }
        }
        return true;
    }
}