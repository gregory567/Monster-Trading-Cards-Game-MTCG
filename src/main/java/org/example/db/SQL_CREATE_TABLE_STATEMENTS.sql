

-- Create User Table
CREATE TABLE IF NOT EXISTS "User" (
    "username" VARCHAR(255) PRIMARY KEY,
    "password" VARCHAR(255) NOT NULL,
    "coins" DOUBLE PRECISION NOT NULL,
    "profile_name" VARCHAR(255),
    "profile_bio" VARCHAR(255),
    "profile_image" VARCHAR(255),
    "elo_score" INTEGER,
    "wins" INTEGER,
    "losses" INTEGER,
    UNIQUE("username")
);

-- Create Card Table
CREATE TABLE IF NOT EXISTS "Card" (
    "id" SERIAL PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "damage" INTEGER NOT NULL,
    "elementType" VARCHAR(50) NOT NULL,
    "specialties" VARCHAR(255) ARRAY,
    "cardType" VARCHAR(50) NOT NULL,
    "owner_username" VARCHAR(255) REFERENCES "User"("username"),
    UNIQUE("id")
);

-- Create Stack Table
CREATE TABLE IF NOT EXISTS "Stack" (
    "username" VARCHAR(255) REFERENCES "User"("username"),
    "card_id" INTEGER REFERENCES "Card"("id"),
    UNIQUE("username", "card_id")
);

-- Create Package Table
CREATE TABLE IF NOT EXISTS "Package" (
    "id" SERIAL PRIMARY KEY,
    "card1_id" INTEGER REFERENCES "Card"("id"),
    "card2_id" INTEGER REFERENCES "Card"("id"),
    "card3_id" INTEGER REFERENCES "Card"("id"),
    "card4_id" INTEGER REFERENCES "Card"("id"),
    "card5_id" INTEGER REFERENCES "Card"("id"),
    UNIQUE("id")
);

-- Create Deck Table
CREATE TABLE IF NOT EXISTS "Deck" (
    "username" VARCHAR(255) REFERENCES "User"("username"),
    "card1_id" INTEGER REFERENCES "Card"("id"),
    "card2_id" INTEGER REFERENCES "Card"("id"),
    "card3_id" INTEGER REFERENCES "Card"("id"),
    "card4_id" INTEGER REFERENCES "Card"("id"),
    UNIQUE("username")
);

-- Create TradeDeal Table
CREATE TABLE IF NOT EXISTS "TradeDeal" (
    "id" SERIAL PRIMARY KEY,
    "offeringUser_username" VARCHAR(255) REFERENCES "User"("username"),
    "offeredCard_id" INTEGER REFERENCES "Card"("id"),
    "requirement_cardType" VARCHAR(50),
    "requirement_minDamage" INTEGER,
    "status" VARCHAR(50) DEFAULT 'PENDING',
    UNIQUE("id")
);

-- Create Battle Table
CREATE TABLE IF NOT EXISTS "Battle" (
    "id" SERIAL PRIMARY KEY,
    "user1_username" VARCHAR(255) REFERENCES "User"("username"),
    "user2_username" VARCHAR(255) REFERENCES "User"("username"),
    "outcome" VARCHAR(255),
    UNIQUE("id")
);

-- Create BattleResult Table
CREATE TABLE IF NOT EXISTS "BattleResult" (
    "battle_id" INTEGER REFERENCES "Battle"("id"),
    "opponent_username" VARCHAR(255) REFERENCES "User"("username"),
    "outcome" VARCHAR(255),
    UNIQUE("battle_id", "opponent_username")
);

-- Create RoundDetail Table
CREATE TABLE IF NOT EXISTS "RoundDetail" (
    "round_id" SERIAL PRIMARY KEY,
    "battle_id" INTEGER REFERENCES "Battle"("id"),
    "card_id" INTEGER REFERENCES "Card"("id"),
    "player_username" VARCHAR(255) REFERENCES "User"("username"),
    UNIQUE("battle_id", "round_id", "card_id")
);

-- Create BattleLog Table
CREATE TABLE IF NOT EXISTS "BattleLog" (
    "battle_id" INTEGER REFERENCES "Battle"("id"),
    "round_number" INTEGER,
    "winner_username" VARCHAR(255) REFERENCES "User"("username"),
    "loser_username" VARCHAR(255) REFERENCES "User"("username"),
    "draw" BOOLEAN,
    "round_id" INTEGER REFERENCES "RoundDetail"("round_id"),
    UNIQUE("battle_id", "round_number")
);