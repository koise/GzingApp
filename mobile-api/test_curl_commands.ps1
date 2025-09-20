# PowerShell CURL Testing Script for Landmarks Mobile API
# This script tests the landmarks API using PowerShell's Invoke-WebRequest

Write-Host "🔍 Testing Landmarks Mobile API with PowerShell" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Green
Write-Host ""

$baseUrl = "https://powderblue-pig-261057.hostingersite.com/mobile-api"

# Function to make API requests and display results
function Test-ApiEndpoint {
    param(
        [string]$Url,
        [string]$Description,
        [string]$Method = "GET",
        [hashtable]$Headers = @{"Content-Type"="application/json"; "Accept"="application/json"}
    )
    
    Write-Host "🧪 Testing: $Description" -ForegroundColor Yellow
    Write-Host "URL: $Url" -ForegroundColor Gray
    Write-Host ""
    
    try {
        $response = Invoke-WebRequest -Uri $Url -Method $Method -Headers $Headers
        $statusCode = $response.StatusCode
        $content = $response.Content | ConvertFrom-Json
        
        Write-Host "✅ Status: $statusCode" -ForegroundColor Green
        
        if ($content.success) {
            Write-Host "✅ API Response: SUCCESS" -ForegroundColor Green
            Write-Host "📊 Data Summary:" -ForegroundColor Cyan
            
            if ($content.data.landmarks) {
                $landmarkCount = $content.data.landmarks.Count
                Write-Host "   📍 Landmarks found: $landmarkCount" -ForegroundColor White
                
                if ($landmarkCount -gt 0) {
                    $firstLandmark = $content.data.landmarks[0]
                    Write-Host "   🏷️  First landmark: $($firstLandmark.name)" -ForegroundColor White
                    Write-Host "   📍 Location: $($firstLandmark.coordinates.latitude), $($firstLandmark.coordinates.longitude)" -ForegroundColor White
                    Write-Host "   🏢 Category: $($firstLandmark.category)" -ForegroundColor White
                }
            }
            
            if ($content.data.pagination) {
                $pagination = $content.data.pagination
                Write-Host "   📄 Page: $($pagination.current_page) of $($pagination.total_pages)" -ForegroundColor White
                Write-Host "   📊 Total items: $($pagination.total_items)" -ForegroundColor White
            }
            
            if ($content.data.filters) {
                $categories = $content.data.filters.categories
                if ($categories) {
                    Write-Host "   🏷️  Available categories: $($categories -join ', ')" -ForegroundColor White
                }
            }
        } else {
            Write-Host "❌ API Response: FAILED" -ForegroundColor Red
            Write-Host "Error: $($content.message)" -ForegroundColor Red
        }
        
    } catch {
        Write-Host "❌ Request Failed" -ForegroundColor Red
        Write-Host "Error: $($_.Exception.Message)" -ForegroundColor Red
        
        if ($_.Exception.Response) {
            $statusCode = $_.Exception.Response.StatusCode
            Write-Host "HTTP Status: $statusCode" -ForegroundColor Red
            
            try {
                $stream = $_.Exception.Response.GetResponseStream()
                $reader = New-Object System.IO.StreamReader($stream)
                $responseBody = $reader.ReadToEnd()
                $errorData = $responseBody | ConvertFrom-Json
                Write-Host "Error Details: $($errorData.message)" -ForegroundColor Red
            } catch {
                Write-Host "Raw Error Response: $responseBody" -ForegroundColor Red
            }
        }
    }
    
    Write-Host ""
    Write-Host "----------------------------------------" -ForegroundColor Gray
    Write-Host ""
}

# Test 1: Health Check
Test-ApiEndpoint -Url "$baseUrl/health" -Description "Health Check"

# Test 2: Get All Active Landmarks
Test-ApiEndpoint -Url "$baseUrl/landmarks" -Description "Get All Active Landmarks"

# Test 3: Get Landmarks with Pagination
Test-ApiEndpoint -Url "$baseUrl/landmarks?page=1&limit=5" -Description "Get Landmarks with Pagination (5 items)"

# Test 4: Search Landmarks
Test-ApiEndpoint -Url "$baseUrl/landmarks?search=LRT" -Description "Search for 'LRT' landmarks"

# Test 5: Filter by Category
Test-ApiEndpoint -Url "$baseUrl/landmarks?category=business" -Description "Filter by 'business' category"

# Test 6: Location-based Search (Antipolo area)
Test-ApiEndpoint -Url "$baseUrl/landmarks?lat=14.6255&lng=121.1756&radius=10" -Description "Location-based search (Antipolo, 10km radius)"

# Test 7: Get Landmarks with All Parameters
Test-ApiEndpoint -Url "$baseUrl/landmarks?page=1&limit=3&search=cafe&category=business" -Description "Complex query with multiple filters"

Write-Host "🎯 Testing Complete!" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Summary:" -ForegroundColor Cyan
Write-Host "- If all tests show 'SUCCESS', the API is working correctly" -ForegroundColor White
Write-Host "- If tests show 'FAILED', check the error messages for specific issues" -ForegroundColor White
Write-Host "- The most common issue is that the landmarks endpoint files need to be uploaded to the server" -ForegroundColor White
Write-Host ""
Write-Host "🔧 Troubleshooting:" -ForegroundColor Yellow
Write-Host "1. Ensure mobile-api/endpoints/landmarks/ directory is uploaded" -ForegroundColor White
Write-Host "2. Ensure the main index.php includes the landmarks route" -ForegroundColor White
Write-Host "3. Check that the database table is named 'landmarks_pin' (not 'landmarks')" -ForegroundColor White
Write-Host "4. Verify database connection and table structure" -ForegroundColor White
