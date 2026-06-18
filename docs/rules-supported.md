# UNO Rules Supported

This document lists which rules from `Final_Project_UNO_rules_reference.md` are implemented in this project, along with any variants or simplifications.

## Deck Composition ✅

- Four colors: Red (R), Yellow (Y), Green (G), Blue (B)
- One `0` card per color
- Two cards for each number `1–9` per color
- Two `Skip` cards per color
- Two `Reverse` cards per color
- Two `Draw Two` cards per color
- Four `Wild` cards
- Four `Wild Draw Four` cards
- **Total: 108 cards**

## Legal Play Validation ✅

A card is legal if:
- Its color matches the current active color
- Its number matches the top card number
- Its action type matches the top card action type (e.g. Skip on Skip)
- It is a Wild
- It is a Wild Draw Four

After a Wild is played, the called color becomes the active color for future checks.

## Skip ✅

- Next player loses their turn
- Play continues with the player after the skipped player
- Works correctly in 2, 3, and 4 player games

## Reverse ✅

- Turn direction changes from clockwise to counterclockwise
- **Two-player simplification:** Reverse acts like Skip — the same player goes again
- This is a documented and widely accepted UNO variant

## Draw Two ✅

- Next player draws two cards and loses their turn
- Play continues with the following player
- **Stacking:** Not implemented. Draw Two cards cannot be stacked

## Wild ✅

- Playable on any card regardless of color or number
- Player (human or bot) chooses the next active color
- Bot chooses the color it has the most of in hand; defaults to Red if all wilds

## Wild Draw Four ✅

- Playable on any card
- Player chooses the next active color
- Next player draws four cards and loses their turn
- **Challenge rule:** Not implemented

## Draw/Pass Behavior ✅

- If no legal card exists, the player draws one card
- If the drawn card is legal, the player may play it immediately
- If the drawn card is not legal, the turn passes
- **Variant used:** draw one card, play immediately if legal — this is the most common casual variant

## UNO Call and Missed-UNO Penalty ✅

- When a player reaches one card, "UNO!" is announced automatically
- **Human players:** must type `UNO` when prompted; failing to do so results in a 2-card penalty
- **Bot players:** always call UNO automatically (no penalty possible)
- Penalty timing: checked immediately after the card is played

## Round Scoring ✅

- Round ends when a player empties their hand
- Round winner receives points from all other players' remaining cards
- Number cards: face value (0–9)
- Skip, Reverse, Draw Two: 20 points each
- Wild, Wild Draw Four: 50 points each

## Multi-Round Game to Target Score ✅

- Game continues across multiple rounds automatically
- Target score: **500 points**
- First player to reach or exceed 500 points wins the game
- No fixed number of rounds — the game runs until the target is hit

## Simplifications and Omissions

| Rule | Status |
|------|--------|
| Wild Draw Four challenge | ❌ Not implemented |
| Draw Two stacking | ❌ Not implemented |
| Starting card is action card | Handled by redrawing until a non-wild card is found |
| UNO penalty for bots | Not applicable — bots auto-call |
