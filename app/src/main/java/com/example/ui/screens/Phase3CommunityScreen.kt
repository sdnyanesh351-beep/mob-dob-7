package com.example.ui.screens

import com.example.ui.components.FullScreenOfflineState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text

import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AlumniMentorEntity
import com.example.data.FeedPostEntity
import com.example.data.ReferralLeaderboardUser
import com.example.data.WalletState

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun Phase3CommunityScreen(
    feedPosts: List<FeedPostEntity>,
    walletState: WalletState,
    alumniMentors: List<AlumniMentorEntity> = emptyList(),
    currentTenant: String,
    leaderboard: List<ReferralLeaderboardUser> = emptyList(),
    profileCompletionPercent: Int = 0,
    leaderboardSource: String = "sample",
    streakSource: String = "local",
    isOnline: Boolean = true,
    onRefresh: (() -> Unit)? = null,
    onAddPost: (String, String, String, List<String>?, String?, String?, String?, Int?) -> Unit,
    onToggleLike: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onVotePoll: (String, String, Int) -> Unit = { _, _, _ -> },
    onToggleEventRegistration: (String) -> Unit = { },
    onAssignRequestToMe: (String) -> Unit = { },
    onShowToast: (String) -> Unit,
    onRedeemCode: suspend (String) -> Pair<Boolean, String> = { Pair(false, "") },
    onPurchaseStreakFreeze: suspend () -> Pair<Boolean, String> = { Pair(false, "") }
) {
    var selectedSubTab by remember { mutableIntStateOf(0) } // 0: Feed, 1: Mentors, 2: Gamification, 3: Wallet
    val subTabs = listOf("Feed", "Alumni", "Gamification", "Wallet")
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 16.dp
        ) {
            subTabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedSubTab == index,
                    onClick = { selectedSubTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                )
            }
        }

        when (selectedSubTab) {
            0 -> FeedSection(
                posts = feedPosts,
                currentTenant = currentTenant,
                isOnline = isOnline,
                onRefresh = if (selectedSubTab == 0) onRefresh else null,
                onAddPost = onAddPost,
                onToggleLike = onToggleLike,
                onAddComment = onAddComment,
                onVotePoll = onVotePoll,
                onToggleEventRegistration = onToggleEventRegistration,
                onAssignRequestToMe = onAssignRequestToMe
            )
            1 -> AlumniMentorsSection(
                mentors = alumniMentors,
                onShowToast = onShowToast
            )
            2 -> GamificationSection(
                    walletState = walletState,
                    leaderboard = leaderboard,
                    profileCompletionPercent = profileCompletionPercent,
                    leaderboardSource = leaderboardSource,
                    streakSource = streakSource,
                    onShowToast = onShowToast
                )
            3 -> WalletScreen(
                walletState = walletState,
                onRedeemCode = onRedeemCode,
                onPurchaseStreakFreeze = onPurchaseStreakFreeze,
                onBack = null,
                showToast = onShowToast,
                coroutineScope = coroutineScope
            )
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FeedSection(
    posts: List<FeedPostEntity>,
    currentTenant: String,
    isOnline: Boolean = true,
    onRefresh: (() -> Unit)? = null,
    onAddPost: (String, String, String, List<String>?, String?, String?, String?, Int?) -> Unit,
    onToggleLike: (String) -> Unit,
    onAddComment: (String, String) -> Unit,
    onVotePoll: (String, String, Int) -> Unit = { _, _, _ -> },
    onToggleEventRegistration: (String) -> Unit = { },
    onAssignRequestToMe: (String) -> Unit = { }
) {
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var activeCommentPostId by remember { mutableStateOf<String?>(null) }
    var isFeedRefreshing by remember { mutableStateOf(false) }

    val tenantPosts = remember(posts, currentTenant) {
        posts.filter { currentTenant == "platform" || it.tenantId == currentTenant || it.tenantId == "platform" }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isFeedRefreshing,
        onRefresh = {
            isFeedRefreshing = true
            onRefresh?.invoke()
            isFeedRefreshing = false
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Create Post Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showCreatePostDialog = true }
                .testTag("create_post_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✍️",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Share an update, question, or win...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Post",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (showCreatePostDialog) {
            CreatePostDialog(
                onDismiss = { showCreatePostDialog = false },
                onSubmit = { title, content, type, pollOptions, eventTitle, eventDate, eventLocation, capacity ->
                    onAddPost(title, content, type, pollOptions, eventTitle, eventDate, eventLocation, capacity)
                    showCreatePostDialog = false
                }
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (!isOnline && tenantPosts.isEmpty()) {
            FullScreenOfflineState(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                onRetry = { onRefresh?.invoke() }
            )
        } else {
            // Posts List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
            if (tenantPosts.isEmpty()) {
                items(3) {
                    com.example.ui.components.SkeletonPlaceholderCard(modifier = Modifier.padding(vertical = 4.dp))
                }
            } else {
                items(tenantPosts, key = { it.id }) { post ->
                var isCommentsExpanded by remember(post.id) { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("post_item_${post.id}"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = post.authorName.take(1),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = post.authorName,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${post.authorRole} • ${post.timestamp}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            // Post Type Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = when (post.type) {
                                    "Question" -> MaterialTheme.colorScheme.errorContainer
                                    "Interview Win" -> Color(0xFFFEF08A) // Light yellow
                                    "Referral Share" -> MaterialTheme.colorScheme.secondaryContainer
                                    "poll" -> MaterialTheme.colorScheme.tertiaryContainer
                                    "event" -> MaterialTheme.colorScheme.primaryContainer
                                    "request" -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ) {
                                Text(
                                    text = when(post.type) {
                                        "poll" -> "Poll"
                                        "event" -> "Event"
                                        "request" -> "Request"
                                        else -> post.type
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = when (post.type) {
                                            "Question" -> MaterialTheme.colorScheme.onErrorContainer
                                            "Interview Win" -> Color(0xFF854D0E) // Dark yellow text
                                            "Referral Share" -> MaterialTheme.colorScheme.onSecondaryContainer
                                            "poll" -> MaterialTheme.colorScheme.onTertiaryContainer
                                            "event" -> MaterialTheme.colorScheme.onPrimary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        fontSize = 10.sp
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = post.content,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Poll Options Display
                        if (post.type == "poll" && post.pollOptions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val hasUserVoted = post.userPollVote != null
                            val totalVotes = post.pollOptions.sumOf { it.votes }.coerceAtLeast(1)
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = if (hasUserVoted) {
                                        "✓ You voted · Tap again to change or undo · $totalVotes total vote${if (totalVotes == 1) "" else "s"}"
                                    } else {
                                        "Tap an option to cast your vote · $totalVotes total vote${if (totalVotes == 1) "" else "s"}"
                                    },
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = if (hasUserVoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                post.pollOptions.forEachIndexed { idx, opt ->
                                    val isMyVote = hasUserVoted && opt.option.equals(post.userPollVote, ignoreCase = true)
                                    val percent = (opt.votes * 100) / totalVotes
                                    val optionBg = if (isMyVote) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(optionBg)
                                            .border(
                                                BorderStroke(
                                                    if (isMyVote) 1.3.dp else 0.8.dp,
                                                    if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(
                                                        alpha = 0.6f
                                                    )
                                                ),
                                                RoundedCornerShape(10.dp)
                                            )
                                            .clickable(enabled = true, onClick = {
                                                onVotePoll(post.id, opt.option, idx)
                                            })
                                            .padding(horizontal = 10.dp, vertical = 10.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isMyVote) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = null,
                                                        tint = if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(10.dp))
                                                    Text(
                                                        text = opt.option,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                                Text(
                                                    text = "${opt.votes} · $percent%",
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        fontWeight = FontWeight.SemiBold
                                                    ),
                                                    color = if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            if (hasUserVoted) {
                                                LinearProgressIndicator(
                                                    progress = { opt.votes.toFloat() / totalVotes.toFloat() },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = if (isMyVote) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Event Card Display
                        if (post.type == "event" && !post.eventTitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = post.eventTitle!!, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                    if (!post.eventDate.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = post.eventDate!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (!post.eventLocation.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = post.eventLocation!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    val attendeesCount = post.attendees.coerceAtLeast(0)
                                    val capacityCount = post.capacity.coerceAtLeast(0)
                                    val isFull = capacityCount > 0 && attendeesCount >= capacityCount && !post.registeredByMe
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Group,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (capacityCount > 0) {
                                                    "$attendeesCount / $capacityCount registered"
                                                } else {
                                                    "$attendeesCount registered"
                                                },
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isFull) Color(0xFFBA1A1A) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            if (isFull) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    color = Color(0xFFBA1A1A).copy(alpha = 0.15f),
                                                    shape = RoundedCornerShape(6.dp)
                                                ) {
                                                    Text(
                                                        text = "FULL",
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color(0xFFBA1A1A)
                                                    )
                                                }
                                            }
                                        }
                                        Button(
                                            onClick = { onToggleEventRegistration(post.id) },
                                            enabled = !isFull,
                                            modifier = Modifier.height(32.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (post.registeredByMe) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primary,
                                                contentColor = if (post.registeredByMe) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimary,
                                                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                            )
                                        ) {
                                            Icon(
                                                imageVector = if (post.registeredByMe) Icons.Default.Close else Icons.Default.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = when {
                                                    isFull -> "Event Full"
                                                    post.registeredByMe -> "Unregister"
                                                    else -> "Register"
                                                },
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Request Card Display (Help / Referral / Appointment Requests)
                        if (post.type == "request") {
                            Spacer(modifier = Modifier.height(8.dp))
                            val statusRaw = post.status?.trim()?.lowercase()
                            val resolvedStatus = when (statusRaw) {
                                "open", "unassigned", "new" -> "Open"
                                "in progress", "in_progress", "assigned", "claimed" -> "In Progress"
                                "completed", "done", "closed", "resolved" -> "Completed"
                                "cancelled", "canceled", "withdrawn" -> "Cancelled"
                                else -> (post.status ?: "Open").trim().ifBlank { "Open" }
                            }
                            val (statusColor, statusBg) = when (resolvedStatus.lowercase()) {
                                "open" -> Color(0xFF2563EB) to Color(0xFFDBEAFE)
                                "in progress" -> Color(0xFFB45309) to Color(0xFFFEF3C7)
                                "completed" -> Color(0xFF059669) to Color(0xFFD1FAE5)
                                "cancelled" -> Color(0xFFBA1A1A) to Color(0xFFFCE7E7)
                                else -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
                            }
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = statusBg.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.HelpOutline,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Request",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Surface(
                                            color = statusBg,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = resolvedStatus.uppercase(),
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = 0.3.sp,
                                                color = statusColor
                                            )
                                        }
                                    }
                                    if (!post.assignedTo.isNullOrBlank()) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Assigned to · ${post.assignedTo}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    val buttonTitle = when (resolvedStatus.lowercase()) {
                                        "completed" -> "Completed"
                                        "cancelled" -> "Cancelled"
                                        else -> "Assign to Me"
                                    }
                                    val isButtonEnabled = resolvedStatus.equals("Open", ignoreCase = true)
                                            && post.assignedTo.isNullOrBlank()
                                    Button(
                                        onClick = { onAssignRequestToMe(post.id) },
                                        enabled = isButtonEnabled,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!post.assignedTo.isNullOrBlank()) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
                                            contentColor = if (!post.assignedTo.isNullOrBlank()) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
                                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                    ) {
                                        Icon(
                                            imageVector = if (!post.assignedTo.isNullOrBlank()) Icons.Default.Check else Icons.Default.PersonAdd,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = buttonTitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Like & Comment Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onToggleLike(post.id) },
                                    modifier = Modifier.testTag("like_button_${post.id}")
                                ) {
                                    Icon(
                                        imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                        contentDescription = "Like",
                                        tint = if (post.isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${post.likesCount}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCommentsExpanded) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            isCommentsExpanded = !isCommentsExpanded
                                            if (isCommentsExpanded) {
                                                activeCommentPostId = post.id
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubbleOutline,
                                            contentDescription = "Comment",
                                            tint = if (isCommentsExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isCommentsExpanded) "Hide (${post.comments.size})" else "${post.comments.size} Comments",
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCommentsExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        if (isCommentsExpanded && post.comments.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    post.comments.forEach { comment ->
                                        Row(
                                            verticalAlignment = Alignment.Top,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(26.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = comment.authorName.take(1),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontSize = 11.sp
                                                    )
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text(
                                                            text = comment.authorName,
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "• ${comment.authorRole}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                                                fontSize = 10.sp
                                                            )
                                                        )
                                                    }
                                                    Text(
                                                        text = comment.timestamp,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                                            fontSize = 10.sp
                                                        )
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = comment.text,
                                                    style = MaterialTheme.typography.bodySmall.copy(
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
        }
        }

        PullRefreshIndicator(
            refreshing = isFeedRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }

    if (activeCommentPostId != null) {
        var commentInput by remember { mutableStateOf("") }
        Dialog(onDismissRequest = { activeCommentPostId = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Add Comment", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = commentInput,
                        onValueChange = { commentInput = it },
                        placeholder = { Text("Write your comment...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { activeCommentPostId = null }) { Text("Cancel") }
                        Button(
                            onClick = {
                                if (commentInput.isNotBlank()) {
                                    onAddComment(activeCommentPostId!!, commentInput)
                                    activeCommentPostId = null
                                }
                            }
                        ) { Text("Post Comment") }
                    }
                }
            }
        }
    }
}

@Composable
private fun GamificationSection(
    walletState: WalletState,
    leaderboard: List<ReferralLeaderboardUser> = emptyList(),
    profileCompletionPercent: Int = 0,
    leaderboardSource: String = "sample",
    streakSource: String = "local",
    onShowToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Level & Streak Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Level ${walletState.level} Scholar",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "${walletState.xp} Total XP Earned",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFF9800)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${walletState.streakDays} Day Streak",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Level Progress (${walletState.xp % 500} / 500 XP to Level ${walletState.level + 1})",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { ((walletState.xp % 500).toFloat() / 500f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Badges Gallery
        Text("Earned Badges", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(walletState.badges) { badge ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = badge, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Leaderboard Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Leaderboard, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tenant Leaderboard", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(12.dp))

                val displayList = if (leaderboard.isNotEmpty()) {
                    leaderboard.take(5)
                } else {
                    listOf(
                        ReferralLeaderboardUser("u-1", 1, "Alex Rivera (You)", 1250, 7, 500, true, 0),
                        ReferralLeaderboardUser("u-2", 2, "Priya Sharma", 1120, 5, 400, false, 1),
                        ReferralLeaderboardUser("u-3", 3, "Rohan Verma", 980, 4, 350, false, 2)
                    )
                }
                displayList.forEach { user ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val badgeColors = listOf(
                                Color(0xFF4F46E5), Color(0xFF7C3AED), Color(0xFF06B6D4),
                                Color(0xFF10B981), Color(0xFFF59E0B)
                            )
                            val badgeColor = badgeColors.getOrElse(user.avatarBadgeIndex) { MaterialTheme.colorScheme.primary }
                            Surface(
                                shape = CircleShape,
                                color = badgeColor,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = user.name.take(1).uppercase(),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${user.rank}. ${user.name}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "%,d XP".format(user.points),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            if (user.referrals > 0) {
                                Text(
                                    text = "🔥 ${user.referrals} Days • %d Coins".format(user.coins),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
                if (leaderboard.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Source: ${leaderboardSource.uppercase()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}

@Composable
private fun WalletSection(
    walletState: WalletState,
    onShowToast: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Wallet Balance Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("In-App Wallet", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(32.dp))
                        Text("${walletState.coins} Coins", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Standard Currency", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        Text("${walletState.flashCoins} Flash Coins", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
                        Text("Premium AI Credits", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Referral & Affiliate Hub
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Referral & Affiliate Link", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Share your code with friends to earn +100 Coins for each signup!", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(walletState.referralCode, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                        IconButton(onClick = { onShowToast("Referral code copied to clipboard!") }) {
                            Icon(Icons.Default.Share, contentDescription = "Share")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlumniMentorsSection(
    mentors: List<AlumniMentorEntity>,
    onShowToast: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedServiceFilter by remember { mutableStateOf("All") }
    var selectedMentorForBooking by remember { mutableStateOf<AlumniMentorEntity?>(null) }

    val filteredMentors = remember(mentors, searchQuery, selectedServiceFilter) {
        mentors.filter { m ->
            val matchesSearch = m.name.contains(searchQuery, ignoreCase = true) ||
                    m.company.contains(searchQuery, ignoreCase = true) ||
                    m.role.contains(searchQuery, ignoreCase = true) ||
                    m.skills.any { it.contains(searchQuery, ignoreCase = true) }
            val matchesService = selectedServiceFilter == "All" || m.availableServices.contains(selectedServiceFilter)
            matchesSearch && matchesService
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🎓 Alumni Mentorship & Referral Network",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                Text(
                    text = "Connect directly with verified alumni at Google, Meta, Starlight AI, and Acme for resume reviews and internal referrals.",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by company, mentor name or skill...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Service Filter Chips
        val services = listOf("All", "Resume Review", "Mock Interview", "Referral", "1-on-1 Mentorship")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(services) { service ->
                val isSelected = selectedServiceFilter == service
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedServiceFilter = service },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = service,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        filteredMentors.forEach { mentor ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("mentor_card_${mentor.id}"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = mentor.name.take(1),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = mentor.name,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${mentor.role} @ ${mentor.company}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${mentor.rating}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = mentor.bio,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Skills Chips
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        mentor.skills.take(3).forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = skill,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Class of '${mentor.graduationYear.takeLast(2)}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Button(
                            onClick = { selectedMentorForBooking = mentor },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Request Session", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Booking Dialog Modal
    selectedMentorForBooking?.let { mentor ->
        Dialog(onDismissRequest = { selectedMentorForBooking = null }) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Request Mentorship with ${mentor.name}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Select a service to request:", style = MaterialTheme.typography.bodySmall)

                    Spacer(modifier = Modifier.height(12.dp))

                    mentor.availableServices.forEach { service ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onShowToast("Session request for '$service' sent to ${mentor.name}!")
                                    selectedMentorForBooking = null
                                },
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(service, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                Text("Select →", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(
                        onClick = { selectedMentorForBooking = null },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, List<String>?, String?, String?, String?, Int?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("text") }
    val postTypes = listOf("text", "poll", "event", "request")

    // Poll options
    var pollOptions by remember { mutableStateOf(listOf("", "")) }

    // Event details
    var eventTitle by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var eventLocation by remember { mutableStateOf("") }
    var capacity by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("create_post_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Create Community Post",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )

                // Post Type Selection Chips
                Column {
                    Text(
                        text = "Post Category",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(postTypes) { type ->
                            val isSelected = selectedType == type
                            val typeLabel = when(type) {
                                "text" -> "Discussion"
                                "poll" -> "Poll"
                                "event" -> "Event"
                                "request" -> "Request"
                                else -> type
                            }
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedType = type }
                                    .testTag("post_type_chip_$type"),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = typeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedType != "text") {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        placeholder = { Text("Give your post a title...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("post_title_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("What do you want to talk about?") },
                    placeholder = { Text("Share details, questions, or resources...") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier.fillMaxWidth().testTag("post_content_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Render dynamic poll options input if selectedType == "poll"
                if (selectedType == "poll") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Poll Options",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        pollOptions.forEachIndexed { index, option ->
                            OutlinedTextField(
                                value = option,
                                onValueChange = { newVal ->
                                    val newList = pollOptions.toMutableList()
                                    newList[index] = newVal
                                    pollOptions = newList
                                },
                                label = { Text("Option ${index + 1}") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("poll_option_input_$index"),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (pollOptions.size < 5) {
                                TextButton(onClick = { pollOptions = pollOptions + "" }) {
                                    Text("+ Add Option")
                                }
                            }
                            if (pollOptions.size > 2) {
                                TextButton(onClick = { pollOptions = pollOptions.dropLast(1) }) {
                                    Text("- Remove Option", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // Render dynamic event inputs if selectedType == "event"
                if (selectedType == "event") {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Event Details",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        OutlinedTextField(
                            value = eventTitle,
                            onValueChange = { eventTitle = it },
                            label = { Text("Event Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("event_title_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = eventDate,
                            onValueChange = { eventDate = it },
                            label = { Text("Date/Time (e.g. 2026-08-15 14:00)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("event_date_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = eventLocation,
                            onValueChange = { eventLocation = it },
                            label = { Text("Location") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("event_location_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        OutlinedTextField(
                            value = capacity,
                            onValueChange = { capacity = it },
                            label = { Text("Capacity (Max attendees)") },
                            singleLine = true,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth().testTag("event_capacity_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                val validationError = remember(selectedType, title, content, pollOptions, eventTitle, eventDate, eventLocation, capacity) {
                    when {
                        selectedType != "text" && title.isBlank() -> "Title cannot be empty."
                        content.isBlank() -> "Content cannot be empty."
                        content.length < 5 -> "Content must be at least 5 characters."
                        selectedType == "poll" && pollOptions.count { it.isNotBlank() } < 2 -> "Please provide at least 2 poll options."
                        selectedType == "event" && eventTitle.isBlank() -> "Event title cannot be empty."
                        selectedType == "event" && eventDate.isBlank() -> "Event date/time is required."
                        selectedType == "event" && eventLocation.isBlank() -> "Event location is required."
                        selectedType == "event" && (capacity.toIntOrNull() ?: 0) <= 0 -> "Event capacity must be a positive number."
                        else -> null
                    }
                }

                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_post_dialog")
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (validationError == null) {
                                val resolvedPollOpts = if (selectedType == "poll") pollOptions.filter { it.isNotBlank() } else null
                                val resolvedCapacity = capacity.toIntOrNull()
                                onSubmit(
                                    if (selectedType == "text") "Discussion" else title.ifBlank { "Discussion" },
                                    content,
                                    selectedType,
                                    resolvedPollOpts,
                                    eventTitle.takeIf { it.isNotBlank() },
                                    eventDate.takeIf { it.isNotBlank() },
                                    eventLocation.takeIf { it.isNotBlank() },
                                    resolvedCapacity
                                )
                            }
                        },
                        enabled = validationError == null,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("submit_post_button")
                    ) {
                        Text("Post")
                    }
                }
            }
        }
    }
}

