#!/bin/bash

################################################################################
# Step 4: Test Authentication Flow (ENHANCED WITH ERROR HANDLING)
################################################################################

set -e

# Color codes for better output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "════════════════════════════════════════════════════════════════"
echo "  STEP 4: Testing Authentication Flow"
echo "════════════════════════════════════════════════════════════════"

BASE_URL="http://localhost:8083/api/auth"

# ═══════════════════════════════════════════════════════════════════
# Function: Check Service Health
# ═══════════════════════════════════════════════════════════════════
check_service() {
    echo -e "${YELLOW}Checking if Auth Service is running...${NC}"

    if curl -s "${BASE_URL}/actuator/health" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ Auth Service is running${NC}"
        return 0
    else
        echo -e "${RED}❌ ERROR: Auth Service is not running${NC}"
        echo "   Run: ./scripts/3-start-auth-service.sh"
        return 1
    fi
}

# ═══════════════════════════════════════════════════════════════════
# Function: Test User Registration
# ═══════════════════════════════════════════════════════════════════
test_registration() {
    echo ""
    echo -e "${YELLOW}📝 Test 1: Registering new user...${NC}"

    REGISTER_RESPONSE=$(curl -s -X POST "${BASE_URL}/register" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "testuser@youthconnect.ug",
        "password": "Test123!@#",
        "firstName": "Test",
        "lastName": "User",
        "phoneNumber": "+256700000001",
        "role": "YOUTH"
      }')

    # Check if registration was successful
    if echo "$REGISTER_RESPONSE" | grep -q '"success":true\|"userId"\|"user_id"'; then
        echo -e "${GREEN}✅ Registration successful${NC}"
        echo "   Response: $REGISTER_RESPONSE" | head -c 200
    else
        echo -e "${YELLOW}⚠️  Registration response:${NC}"
        echo "   $REGISTER_RESPONSE" | head -c 300
    fi
}

# ═══════════════════════════════════════════════════════════════════
# Function: Test User Login
# ═══════════════════════════════════════════════════════════════════
test_login() {
    echo ""
    echo -e "${YELLOW}🔐 Test 2: Testing login...${NC}"

    LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/login" \
      -H "Content-Type: application/json" \
      -d '{
        "identifier": "testuser@youthconnect.ug",
        "password": "Test123!@#"
      }')

    # Extract access token from response
    if echo "$LOGIN_RESPONSE" | grep -q '"accessToken"\|"access_token"'; then
        echo -e "${GREEN}✅ Login successful${NC}"

        # Try multiple token field names
        ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)

        if [ -z "$ACCESS_TOKEN" ]; then
            ACCESS_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
        fi

        if [ -n "$ACCESS_TOKEN" ]; then
            echo "   Token: ${ACCESS_TOKEN:0:50}..."

            # Test token validation
            test_token_validation "$ACCESS_TOKEN"
        else
            echo -e "${YELLOW}⚠️  Could not extract access token${NC}"
        fi
    else
        echo -e "${RED}❌ Login failed${NC}"
        echo "   Response: $LOGIN_RESPONSE" | head -c 300
        return 1
    fi
}

# ═══════════════════════════════════════════════════════════════════
# Function: Test Token Validation
# ═══════════════════════════════════════════════════════════════════
test_token_validation() {
    local token=$1

    echo ""
    echo -e "${YELLOW}🔍 Test 3: Validating token...${NC}"

    VALIDATE_RESPONSE=$(curl -s -X GET "${BASE_URL}/validate" \
      -H "Authorization: Bearer $token")

    if echo "$VALIDATE_RESPONSE" | grep -q '"valid":true\|"isValid":true\|"success":true'; then
        echo -e "${GREEN}✅ Token validation successful${NC}"
    else
        echo -e "${YELLOW}⚠️  Token validation response:${NC}"
        echo "   $VALIDATE_RESPONSE" | head -c 200
    fi
}

# ═══════════════════════════════════════════════════════════════════
# Function: Test Password Reset Request
# ═══════════════════════════════════════════════════════════════════
test_password_reset() {
    echo ""
    echo -e "${YELLOW}🔑 Test 4: Testing password reset request...${NC}"

    RESET_RESPONSE=$(curl -s -X POST "${BASE_URL}/password/forgot" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "testuser@youthconnect.ug"
      }')

    if echo "$RESET_RESPONSE" | grep -q '"success":true'; then
        echo -e "${GREEN}✅ Password reset request successful${NC}"
    else
        echo -e "${YELLOW}⚠️  Password reset response:${NC}"
        echo "   $RESET_RESPONSE" | head -c 200
    fi
}

# ═══════════════════════════════════════════════════════════════════
# Main Test Execution
# ═══════════════════════════════════════════════════════════════════

# Check if service is running
if ! check_service; then
    exit 1
fi

# Run all tests
test_registration
test_login
test_password_reset

echo ""
echo "════════════════════════════════════════════════════════════════"
echo -e "${GREEN}  Authentication Flow Test Complete${NC}"
echo "════════════════════════════════════════════════════════════════"