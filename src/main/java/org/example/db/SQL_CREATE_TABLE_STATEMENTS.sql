

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

-- Create UserCredentials Table
CREATE TABLE IF NOT EXISTS "UserCredentials" (
    "username" VARCHAR(255) PRIMARY KEY,
    "password" VARCHAR(255) NOT NULL,
    "token" VARCHAR(255)
);

-- Create UserData Table
CREATE TABLE IF NOT EXISTS "UserData" (
    "username" VARCHAR(255) PRIMARY KEY REFERENCES "UserCredentials"("username"),
    "name" VARCHAR(255),
    "bio" VARCHAR(255),
    "image" VARCHAR(255),
    "coins" DOUBLE PRECISION NOT NULL
);

-- Create UserStats Table
CREATE TABLE IF NOT EXISTS "UserStats" (
    "username" VARCHAR(255) PRIMARY KEY REFERENCES "UserData"("username"),
    "elo" INTEGER,
    "wins" INTEGER,
    "losses" INTEGER
);

-- Create Card Table
CREATE TABLE IF NOT EXISTS "Card" (
    "id" UUID PRIMARY KEY,
    "name" VARCHAR(255) NOT NULL,
    "damage" DOUBLE PRECISION NOT NULL,
    "elementType" VARCHAR(50) NOT NULL,
    "specialties" VARCHAR(255) ARRAY,
    "cardType" VARCHAR(50) NOT NULL,
    "owner_username" VARCHAR(255) REFERENCES "User"("username"),
    UNIQUE("id")
);

-- Create Stack Table
CREATE TABLE IF NOT EXISTS "Stack" (
    "username" VARCHAR(255) REFERENCES "User"("username"),
    "card_id" UUID REFERENCES "Card"("id"),
    UNIQUE("username", "card_id")
);

-- Create Package Table
CREATE TABLE IF NOT EXISTS "Package" (
    "id" UUID PRIMARY KEY,
    "card1_id" UUID REFERENCES "Card"("id"),
    "card2_id" UUID REFERENCES "Card"("id"),
    "card3_id" UUID REFERENCES "Card"("id"),
    "card4_id" UUID REFERENCES "Card"("id"),
    "card5_id" UUID REFERENCES "Card"("id"),
    UNIQUE("id")
);

-- Create Deck Table
CREATE TABLE IF NOT EXISTS "Deck" (
    "username" VARCHAR(255) REFERENCES "User"("username"),
    "card1_id" UUID REFERENCES "Card"("id"),
    "card2_id" UUID REFERENCES "Card"("id"),
    "card3_id" UUID REFERENCES "Card"("id"),
    "card4_id" UUID REFERENCES "Card"("id"),
    UNIQUE("username")
);

-- Create TradeDeal Table
CREATE TABLE IF NOT EXISTS "TradeDeal" (
    "id" UUID PRIMARY KEY,
    "offeringUser_username" VARCHAR(255) REFERENCES "User"("username"),
    "offeredCard_id" UUID REFERENCES "Card"("id"),
    "requirement_cardType" VARCHAR(50),
    "requirement_minDamage" DOUBLE PRECISION,
    "status" VARCHAR(50) DEFAULT 'PENDING',
    UNIQUE("id")
);

-- Create Battle Table
CREATE TABLE IF NOT EXISTS "Battle" (
    "id" UUID PRIMARY KEY,
    "user1_username" VARCHAR(255) REFERENCES "User"("username"),
    "user2_username" VARCHAR(255) REFERENCES "User"("username"),
    "outcome" VARCHAR(255),
    UNIQUE("id")
);

-- Create BattleResult Table
CREATE TABLE IF NOT EXISTS "BattleResult" (
    "battle_id" UUID REFERENCES "Battle"("id"),
    "opponent_username" VARCHAR(255) REFERENCES "User"("username"),
    "outcome" VARCHAR(255),
    UNIQUE("battle_id", "opponent_username")
);

-- Create RoundDetail Table
CREATE TABLE IF NOT EXISTS "RoundDetail" (
    "round_id" UUID PRIMARY KEY,
    "battle_id" UUID REFERENCES "Battle"("id"),
    "card_id" UUID REFERENCES "Card"("id"),
    "player_username" VARCHAR(255) REFERENCES "User"("username"),
    UNIQUE("battle_id", "round_id", "card_id")
);

-- Create BattleLog Table
CREATE TABLE IF NOT EXISTS "BattleLog" (
    "battle_id" UUID REFERENCES "Battle"("id"),
    "round_number" INTEGER,
    "winner_username" VARCHAR(255) REFERENCES "User"("username"),
    "loser_username" VARCHAR(255) REFERENCES "User"("username"),
    "draw" BOOLEAN,
    "round_id" UUID REFERENCES "RoundDetail"("round_id"),
    UNIQUE("battle_id", "round_number")
);