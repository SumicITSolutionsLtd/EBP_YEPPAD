#!/bin/bash

################################################################################
# Complete Integration Test Suite
################################################################################

set -e

echo "════════════════════════════════════════════════════════════════"
echo "  Youth Connect Platform - Integration Test Suite"
echo "════════════════════════════════════════════════════════════════"

TEST_RESULTS=()
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Test result tracking
add_test_result() {
    local test_name=$1
    local status=$2
    local message=$3

    TOTAL_TESTS=$((TOTAL_TESTS + 1))

    if [ "$status" == "PASS" ]; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
        echo "✅ $test_name: PASSED"
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        echo "❌ $test_name: FAILED - $message"
    fi

    TEST_RESULTS+=("$test_name|$status|$message")
}

# Test 1: Service Registry
test_eureka() {
    echo ""
    echo "📊 Testing Service Registry (Eureka)..."

    if curl -s http://localhost:8761/actuator/health | grep -q '"status":"UP"'; then
        add_test_result "Eureka Health" "PASS" ""
    else
        add_test_result "Eureka Health" "FAIL" "Service not responding"
        return
    fi

    if curl -s http://localhost:8761/eureka/apps | grep -q "application"; then
        add_test_result "Eureka Apps API" "PASS" ""
    else
        add_test_result "Eureka Apps API" "FAIL" "No applications registered"
    fi
}

# Test 2: Database Connectivity
test_databases() {
    echo ""
    echo "🗄️  Testing Database Connectivity..."

    DATABASES=(
        "youthconnect_auth"
        "youthconnect_user"
        "youthconnect_job"
        "youthconnect_notification"
        "youthconnect_file"
    )

    for db in "${DATABASES[@]}"; do
        if PGPASSWORD=YouthConnect2024! psql -h localhost -U youthconnect_user -d $db -c "SELECT 1" > /dev/null 2>&1; then
            add_test_result "Database: $db" "PASS" ""
        else
            add_test_result "Database: $db" "FAIL" "Connection failed"
        fi
    done
}

# Test 3: Authentication Flow
test_auth_flow() {
    echo ""
    echo "🔐 Testing Authentication Flow..."

    BASE_URL="http://localhost:8083/api/auth"

    # Check service health
    if ! curl -s $BASE_URL/actuator/health > /dev/null 2>&1; then
        add_test_result "Auth Service Health" "FAIL" "Service not running"
        return
    fi
    add_test_result "Auth Service Health" "PASS" ""

    # Test registration
    REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/register \
        -H "Content-Type: application/json" \
        -d '{
            "email": "integration.test@youthconnect.ug",
            "password": "Test123!@#",
            "firstName": "Integration",
            "lastName": "Test",
            "phoneNumber": "+256700999999",
            "role": "YOUTH"
        }')

    if echo $REGISTER_RESPONSE | grep -q "userId\|user_id\|success"; then
        add_test_result "User Registration" "PASS" ""
    else
        add_test_result "User Registration" "FAIL" "Registration failed"
    fi

    # Test login
    LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/login \
        -H "Content-Type: application/json" \
        -d '{
            "email": "integration.test@youthconnect.ug",
            "password": "Test123!@#"
        }')

    if echo $LOGIN_RESPONSE | grep -q "accessToken\|access_token"; then
        add_test_result "User Login" "PASS" ""

        # Extract token
        ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"accessToken":"[^"]*' | cut -d'"' -f4)
        if [ -z "$ACCESS_TOKEN" ]; then
            ACCESS_TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)
        fi

        # Test token validation
        VALIDATE_RESPONSE=$(curl -s -X POST $BASE_URL/validate \
            -H "Content-Type: application/json" \
            -d "{\"token\": \"$ACCESS_TOKEN\"}")

        if echo $VALIDATE_RESPONSE | grep -q "valid.*true\|isValid.*true"; then
            add_test_result "Token Validation" "PASS" ""
        else
            add_test_result "Token Validation" "FAIL" "Token invalid"
        fi
    else
        add_test_result "User Login" "FAIL" "Login failed"
    fi
}

# Test 4: User Service
test_user_service() {
    echo ""
    echo "👤 Testing User Service..."

    BASE_URL="http://localhost:8081"

    if curl -s $BASE_URL/actuator/health | grep -q '"status":"UP"'; then
        add_test_result "User Service Health" "PASS" ""
    else
        add_test_result "User Service Health" "FAIL" "Service not responding"
    fi
}

# Test 5: Job Service
test_job_service() {
    echo ""
    echo "💼 Testing Job Service..."

    BASE_URL="http://localhost:8000"

    if curl -s $BASE_URL/actuator/health | grep -q '"status":"UP"'; then
        add_test_result "Job Service Health" "PASS" ""
    else
        add_test_result "Job Service Health" "FAIL" "Service not responding"
    fi
}

# Test 6: Notification Service
test_notification_service() {
    echo ""
    echo "📧 Testing Notification Service..."

    BASE_URL="http://localhost:7077"

    if curl -s $BASE_URL/actuator/health | grep -q '"status":"UP"'; then
        add_test_result "Notification Service Health" "PASS" ""
    else
        add_test_result "Notification Service Health" "FAIL" "Service not responding"
    fi
}

# Test 7: File Management Service
test_file_service() {
    echo ""
    echo "📁 Testing File Management Service..."

    BASE_URL="http://localhost:8089"

    if curl -s $BASE_URL/actuator/health | grep -q '"status":"UP"'; then
        add_test_result "File Service Health" "PASS" ""
    else
        add_test_result "File Service Health" "FAIL" "Service not responding"
    fi
}

# Run all tests
test_eureka
test_databases
test_auth_flow
test_user_service
test_job_service
test_notification_service
test_file_service

# Print summary
echo ""
echo "════════════════════════════════════════════════════════════════"
echo "  Test Summary"
echo "════════════════════════════════════════════════════════════════"
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $FAILED_TESTS"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo "✅ All tests passed!"
    exit 0
else
    echo "❌ Some tests failed. Review the output above."
    exit 1
fi