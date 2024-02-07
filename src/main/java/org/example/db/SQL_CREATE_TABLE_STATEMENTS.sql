


-- Create User Table
CREATE TABLE IF NOT EXISTS "User" (
    "username" VARCHAR(255) PRIMARY KEY,
    "password" VARCHAR(255) NOT NULL,
    "token" VARCHAR(255) NOT NULL,
    "profile_name" VARCHAR(255),
    "profile_bio" VARCHAR(255),
    "profile_image" VARCHAR(255),
    "coins" DOUBLE PRECISION NOT NULL,
    "elo_score" INTEGER NOT NULL,
    "wins" INTEGER NOT NULL,
    "losses" INTEGER NOT NULL,
    UNIQUE("username")
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
    UNIQUE("id")
);

-- Create RoundDetail Table
CREATE TABLE IF NOT EXISTS "RoundDetail" (
    "round_id" UUID PRIMARY KEY,
    "winner_card_id" UUID REFERENCES "Card"("id"),
    "winner_card_name" VARCHAR(255),
    "winner_player_username" VARCHAR(255) REFERENCES "User"("username"),
    "loser_card_id" UUID REFERENCES "Card"("id"),
    "loser_card_name" VARCHAR(255),
    "loser_player_username" VARCHAR(255) REFERENCES "User"("username"),
    UNIQUE("round_id", "winner_card_id")
);

-- if the above doesnt work:
-- Create RoundDetail Table (Simple Structure)
CREATE TABLE IF NOT EXISTS "RoundDetail" (
    "round_id" UUID PRIMARY KEY,
    "winner_card_id" UUID,
    "winner_card_name" VARCHAR(255),
    "winner_player_username" VARCHAR(255),
    "loser_card_id" UUID,
    "loser_card_name" VARCHAR(255),
    "loser_player_username" VARCHAR(255),
    UNIQUE("round_id", "winner_card_id")
);

-- Add foreign key constraint for "winner_card_id"
ALTER TABLE "RoundDetail"
ADD CONSTRAINT "fk_rounddetail_winnercardid"
FOREIGN KEY ("winner_card_id")
REFERENCES "Card" ("id");

-- Add foreign key constraint for "winner_player_username"
ALTER TABLE "RoundDetail"
ADD CONSTRAINT "fk_rounddetail_winneruser"
FOREIGN KEY ("winner_player_username")
REFERENCES "User" ("username");

-- Add foreign key constraint for "loser_card_id"
ALTER TABLE "RoundDetail"
ADD CONSTRAINT "fk_rounddetail_losercardid"
FOREIGN KEY ("loser_card_id")
REFERENCES "Card" ("id");

-- Add foreign key constraint for "loser_player_username"
ALTER TABLE "RoundDetail"
ADD CONSTRAINT "fk_rounddetail_loseruser"
FOREIGN KEY ("loser_player_username")
REFERENCES "User" ("username");


-- Create RoundLog Table
CREATE TABLE IF NOT EXISTS "RoundLog" (
    "battle_id" UUID REFERENCES "Battle"("id"),
    "round_number" INTEGER,
    "winner_username" VARCHAR(255) REFERENCES "User"("username"),
    "loser_username" VARCHAR(255) REFERENCES "User"("username"),
    "draw" BOOLEAN,
    "round_id" UUID REFERENCES "RoundDetail"("round_id"),
    UNIQUE("battle_id", "round_number")
);