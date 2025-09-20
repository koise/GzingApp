package com.example.gzingapp.network

import com.example.gzingapp.data.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // System endpoints
    @GET("health")
    suspend fun healthCheck(): Response<ApiResponse<HealthCheckResponse>>
    
    @GET("info")
    suspend fun getApiInfo(): Response<ApiResponse<ApiInfoResponse>>
    
    // Authentication endpoints (session-based)
    @POST("auth/login.php")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/signup.php")
    suspend fun signup(@Body signupRequest: SignupRequest): Response<ApiResponse<AuthResponse>>
    
    @POST("auth/logout.php")
    suspend fun logout(): Response<ApiResponse<Any>>
    
    @GET("auth/check.php")
    suspend fun checkSession(): Response<ApiResponse<SessionCheckResponse>>
    
    // User endpoints
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("role") role: String? = null,
        @Query("status") status: String? = null
    ): Response<ApiResponse<UsersResponse>>
    
    @POST("users")
    suspend fun createUser(@Body userRequest: CreateUserRequest): Response<ApiResponse<User>>
    
    // Routes endpoints - Updated to use new API structure
    @GET("mobile-api/routes")
    suspend fun getRoutes(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("status") status: String? = null
    ): Response<RoutesApiResponse>
    
    // Landmarks endpoints
    @GET("endpoints/landmarks/")
    suspend fun getLandmarks(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50,
        @Query("search") search: String? = null,
        @Query("category") category: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
        @Query("radius") radius: Double? = null
    ): Response<LandmarksResponse>
    
    
    // SOS Contacts endpoints
    @GET("sos/get_contacts.php")
    suspend fun getSosContacts(
        @Query("user_id") userId: Int,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("search") search: String? = null,
        @Query("relationship") relationship: String? = null,
        @Query("is_primary") isPrimary: Boolean? = null
    ): Response<ApiResponse<SosContactsResponse>>

    @POST("sos/create_contact.php")
    suspend fun createSosContact(@Body sosContactRequest: CreateSosContactRequest): Response<ApiResponse<SosContactResponse>>
    
    // New Emergency Contact Management endpoints
    @GET("sos/getUserEmergencyContacts.php")
    suspend fun getUserEmergencyContacts(@Query("user_id") userId: Int): Response<ApiResponse<EmergencyContactsResponse>>
    
    @GET("sos/getEmergencyContact.php")
    suspend fun getEmergencyContact(@Query("contact_id") contactId: Int): Response<ApiResponse<EmergencyContactDetailResponse>>
    
    @POST("sos/updateEmergencyContact.php")
    suspend fun updateEmergencyContact(@Body updateRequest: UpdateEmergencyContactRequest): Response<ApiResponse<EmergencyContactResponse>>
    
    @POST("sos/deleteEmergencyContact.php")
    suspend fun deleteEmergencyContact(@Body deleteRequest: DeleteEmergencyContactRequest): Response<ApiResponse<DeleteEmergencyContactResponse>>
    
    // User Profile endpoints
    @GET("users/get_user_profile.php")
    suspend fun getUserProfile(@Query("user_id") userId: Int): Response<ApiResponse<UserProfileResponse>>
    
    @POST("users/update_user.php")
    suspend fun updateUserProfile(@Body updateRequest: UpdateUserRequest): Response<ApiResponse<UserResponse>>
    
    @POST("users/change_password.php")
    suspend fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Response<ApiResponse<ChangePasswordResponse>>
    
    @POST("users/delete_user.php")
    suspend fun deleteUser(@Body deleteUserRequest: DeleteUserRequest): Response<ApiResponse<DeleteUserResponse>>
    
    // Prototype API endpoints (no authentication required)
    @GET("prototype/updateUsers")
    suspend fun getUpdateUsers(@Query("id") userId: Int): Response<ApiResponse<UpdateUsersResponse>>
    
    @POST("prototype/updateUsers")
    suspend fun updateUsers(@Body updateUsersRequest: UpdateUsersRequest): Response<ApiResponse<UpdateUsersResponse>>
    
    // Navigation Activity Logs endpoints
    @GET("navigation-logs")
    suspend fun getNavigationLogs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("activity_type") activityType: String? = null,
        @Query("transport_mode") transportMode: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<ApiResponse<NavigationLogsResponse>>
    
    @POST("navigation-logs")
    suspend fun createNavigationLog(@Body logRequest: CreateNavigationLogRequest): Response<ApiResponse<NavigationActivityLog>>
    
    @POST("navigation_activity_logs/stop")
    suspend fun logNavigationStop(@Body request: NavigationStopRequest): Response<ApiResponse<NavigationStopResponse>>
    
    @POST("navigation_activity_logs/destination-reached")
    suspend fun logDestinationReached(@Body request: DestinationReachedRequest): Response<NavigationLogsResponse>
    
    @POST("navigation_activity_logs/pause")
    suspend fun logNavigationPause(@Body request: NavigationPauseRequest): Response<NavigationLogsResponse>
    
    @POST("navigation_activity_logs/resume")
    suspend fun logNavigationResume(@Body request: NavigationResumeRequest): Response<NavigationLogsResponse>
    
    @POST("navigation_activity_logs/route-change")
    suspend fun logRouteChange(@Body request: RouteChangeRequest): Response<NavigationLogsResponse>
    
    @GET("navigation_activity_logs/logs")
    suspend fun getNavigationLogs(
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 20,
        @Query("user_id") userId: Int? = null,
        @Query("user_name") userName: String? = null,
        @Query("activity_type") activityType: String? = null,
        @Query("transport_mode") transportMode: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<NavigationLogsResponse>
    
    @GET("navigation_activity_logs/user-logs")
    suspend fun getUserNavigationLogs(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 100
    ): Response<NavigationLogsResponse>
    
    @GET("navigation_activity_logs/stats")
    suspend fun getNavigationStats(
        @Query("user_id") userId: Int,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<NavigationStatsResponse>
    
    @GET("navigation_activity_logs/popular-destinations")
    suspend fun getPopularDestinations(
        @Query("limit") limit: Int = 10
    ): Response<PopularDestinationsResponse>
    
    @GET("navigation_activity_logs/transport-stats")
    suspend fun getTransportStats(): Response<TransportStatsResponse>
    
    @DELETE("navigation_activity_logs/logs")
    suspend fun deleteNavigationLogs(@Body request: DeleteLogsRequest): Response<ApiResponse<Any>>
    
    // Routes endpoints - Updated to use new API structure
    @GET("mobile-api/routes")
    suspend fun getRoutes(): Response<RoutesResponse>
    
    @GET("mobile-api/routes/get_route.php")
    suspend fun getRouteDetails(@Query("id") routeId: Int): Response<com.example.gzingapp.data.RouteDetailsApiResponse>
    
    @POST("mobile-api/endpoints/routes/create_route.php")
    suspend fun createRoute(@Body routeRequest: CreateRouteRequest): Response<CreateRouteResponse>
    
    @GET("routes/{id}/pins")
    suspend fun getRoutePins(@Path("id") routeId: Int): Response<ApiResponse<List<Pin>>>
    
    // Logs API endpoints
    @POST("logs/create_log_test.php")
    suspend fun createLog(@Body request: CreateLogRequest): Response<ApiResponse<CreateLogResponse>>
    
    @GET("logs/get_logs.php")
    suspend fun getLogs(
        @Query("log_type") logType: String = "all",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("activity_type") activityType: String? = null,
        @Query("transport_mode") transportMode: String? = null,
        @Query("log_type_activity") logTypeActivity: String? = null,
        @Query("log_level") logLevel: String? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null
    ): Response<ApiResponse<LogsResponse>>
    
    // New Navigation API endpoints (using the new user_navigation_logs table)
    @POST("mobile-api/endpoints/navigation")
    suspend fun createNewNavigationLog(@Body request: CreateNewNavigationLogRequest): Response<ApiResponse<NavigationLogResponse>>
    
    @GET("mobile-api/endpoints/navigation")
    suspend fun getNavigationLogs(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("activity_type") activityType: String? = null,
        @Query("transport_mode") transportMode: String? = null,
        @Query("destination_reached") destinationReached: Boolean? = null,
        @Query("date_from") dateFrom: String? = null,
        @Query("date_to") dateTo: String? = null,
        @Query("include_details") includeDetails: Boolean = false
    ): Response<ApiResponse<NavigationLogsResponse>>
    
    @GET("mobile-api/endpoints/navigation/stats")
    suspend fun getNavigationStats(
        @Query("period") period: String = "all",
        @Query("include_destinations") includeDestinations: Boolean = true,
        @Query("include_transport_modes") includeTransportModes: Boolean = true
    ): Response<ApiResponse<NavigationStatsResponse>>
    
    @GET("mobile-api/endpoints/navigation/{logId}")
    suspend fun getNavigationLogDetail(@Path("logId") logId: Int): Response<ApiResponse<NavigationLogDetailResponse>>
    
    @PUT("mobile-api/endpoints/navigation/{logId}")
    suspend fun updateNavigationLog(@Path("logId") logId: Int, @Body request: UpdateNavigationLogRequest): Response<ApiResponse<NavigationLogResponse>>
    
    @POST("mobile-api/endpoints/navigation/stop")
    suspend fun stopNavigation(@Body request: StopNavigationRequest): Response<ApiResponse<StopNavigationResponse>>
    
    // Navigation History endpoints (standalone)
    @GET("navigation-history")
    suspend fun getNavigationHistory(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("order_by") orderBy: String = "completion_time",
        @Query("order_direction") orderDirection: String = "DESC"
    ): Response<NavigationHistoryResponse>
    
    @GET("navigation-history/stats")
    suspend fun getNavigationHistoryStats(@Query("user_id") userId: Int): Response<NavigationHistoryResponse>
    
    @POST("mobile-api/endpoints/navigation-history/create_navigation_history_standalone.php")
    suspend fun createNavigationHistory(@Body request: CreateNavigationHistoryRequest): Response<NavigationHistoryResponse>

    @GET("navigation-history/{id}")
    suspend fun getNavigationHistoryById(
        @Path("id") id: Int,
        @Query("user_id") userId: Int? = null
    ): Response<NavigationHistoryResponse>

    // Emergency SMS endpoints
    @POST("sms/send_emergency_sms")
    suspend fun sendEmergencySMS(
        @Body request: EmergencySMSRequest
    ): Response<EmergencySMSResponse>
    
    // Navigation Routes endpoints
    @POST("endpoints/navigation-routes/create_navigation_route.php")
    suspend fun createNavigationRoute(
        @Body request: CreateNavigationRouteRequest
    ): Response<CreateNavigationRouteResponse>
    
    @GET("endpoints/navigation-routes/get_navigation_routes.php")
    suspend fun getNavigationRoutes(
        @Query("user_id") userId: Int,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("order_by") orderBy: String = "created_at",
        @Query("order_direction") orderDirection: String = "DESC",
        @Query("favorites_only") favoritesOnly: Boolean? = null,
        @Query("search") search: String = ""
    ): Response<GetNavigationRoutesResponse>
}
