package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.example.data.BlogPostEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Phase6BlogScreen(
    blogPosts: List<BlogPostEntity>,
    onRefresh: () -> Unit,
    onCreateBlogPost: (String, String, String, List<String>, String?) -> Unit,
    onToggleBookmark: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("All") }
    var activeReadPost by remember { mutableStateOf<BlogPostEntity?>(null) }
    var isCreateDialogOpen by remember { mutableStateOf(false) }

    // Dynamically resolve tags
    val tags = remember(blogPosts) {
        val uniqueTags = blogPosts.flatMap { it.tags }.distinct().sorted()
        listOf("All") + uniqueTags
    }

    // Filter posts
    val filteredPosts = remember(blogPosts, searchQuery, selectedTag) {
        blogPosts.filter { post ->
            val matchesSearch = searchQuery.isBlank() ||
                    post.title.contains(searchQuery, ignoreCase = true) ||
                    post.excerpt.contains(searchQuery, ignoreCase = true) ||
                    post.author.contains(searchQuery, ignoreCase = true)
            val matchesTag = selectedTag == "All" || post.tags.contains(selectedTag)
            matchesSearch && matchesTag
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Blog & Insights",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isCreateDialogOpen = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("create_blog_post_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create BlogPost")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search articles...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("blog_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Tags Row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                items(tags) { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("tag_filter_$tag")
                    )
                }
            }

            // Posts List
            if (blogPosts.isEmpty()) {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(3) {
                        com.example.ui.components.SkeletonPlaceholderCard()
                    }
                }
            } else if (filteredPosts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No articles found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredPosts) { post ->
                        BlogPostCardItem(
                            post = post,
                            onClick = { activeReadPost = post },
                            onToggleBookmark = { onToggleBookmark(post.id) }
                        )
                    }
                }
            }
        }
    }

    // Read BlogPost Dialog
    activeReadPost?.let { post ->
        BlogPostReadDialog(
            post = post,
            onDismiss = { activeReadPost = null },
            onToggleBookmark = { onToggleBookmark(post.id) }
        )
    }

    // Create BlogPost Dialog
    if (isCreateDialogOpen) {
        BlogPostCreateDialog(
            onDismiss = { isCreateDialogOpen = false },
            onSubmit = { title, content, excerpt, tagsList, imageUrl ->
                onCreateBlogPost(title, content, excerpt, tagsList, imageUrl)
                isCreateDialogOpen = false
            }
        )
    }
}

@Composable
fun BlogPostCardItem(
    post: BlogPostEntity,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("blog_post_card_${post.id}"),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            // Optional Image
            val painter = rememberAsyncImagePainter(post.imageUrl ?: "https://placehold.co/800x400.png")
            Image(
                painter = painter,
                contentDescription = post.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Author & Date
                    Text(
                        text = "By ${post.author} • ${post.date.take(10)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Bookmark Icon (we don't have user ID handy here, so we assume if bookmarkedBy is not empty, it's bookmarked, or we can toggle bookmark directly)
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("bookmark_post_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (post.bookmarkedBy.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (post.bookmarkedBy.isNotEmpty()) Color(0xFFFBC02D) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.excerpt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tags Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    post.tags.take(3).forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag, fontSize = 10.sp) },
                            modifier = Modifier.height(24.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogPostReadDialog(
    post: BlogPostEntity,
    onDismiss: () -> Unit,
    onToggleBookmark: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Image & Back Button Overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    val painter = rememberAsyncImagePainter(post.imageUrl ?: "https://placehold.co/800x400.png")
                    Image(
                        painter = painter,
                        contentDescription = post.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay for title readability if needed, or back button block
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.6f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f)
                                    )
                                )
                            )
                    )

                    // Navigation Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .testTag("close_read_dialog_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }

                        IconButton(
                            onClick = onToggleBookmark,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(50))
                                .testTag("bookmark_read_dialog_button")
                        ) {
                            Icon(
                                imageVector = if (post.bookmarkedBy.isNotEmpty()) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (post.bookmarkedBy.isNotEmpty()) Color(0xFFFBC02D) else Color.White
                            )
                        }
                    }

                    // Title on bottom of image card
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Author block
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = post.author,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Published: ${post.date.take(10)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Content Reader
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        // Excerpt in italic style
                        Text(
                            text = post.excerpt,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider()
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    item {
                        Text(
                            text = post.content,
                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 24.sp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("blog_post_full_content")
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        // Tags wrap block
                        Text(
                            text = "Tags",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            post.tags.forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text(tag) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlogPostCreateDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String, List<String>, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var excerpt by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var tagsStr by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create New Blog Post", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_blog_title_input")
                )

                OutlinedTextField(
                    value = excerpt,
                    onValueChange = { excerpt = it },
                    label = { Text("Short Excerpt") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_blog_excerpt_input")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Full Article Content") },
                    minLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_blog_content_input")
                )

                OutlinedTextField(
                    value = tagsStr,
                    onValueChange = { tagsStr = it },
                    label = { Text("Tags (comma separated)") },
                    singleLine = true,
                    placeholder = { Text("Interview, Prep, Resume") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_blog_tags_input")
                )

                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Cover Image URL (optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_blog_image_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank() && excerpt.isNotBlank()) {
                        val tagsList = tagsStr.split(",")
                            .map { it.trim() }
                            .filter { it.isNotBlank() }
                        onSubmit(
                            title,
                            content,
                            excerpt,
                            tagsList,
                            imageUrl.takeIf { it.isNotBlank() }
                        )
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank() && excerpt.isNotBlank(),
                modifier = Modifier.testTag("submit_create_blog_button")
            ) {
                Text("Publish")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

// Simple rememberScrollState helper since we import it implicitly or need it for scrolling inside column
@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}
