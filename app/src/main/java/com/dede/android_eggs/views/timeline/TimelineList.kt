@file:OptIn(ExperimentalMaterial3Api::class)

package com.dede.android_eggs.views.timeline

import android.os.Build
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Android
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dede.android_eggs.R
import com.dede.android_eggs.main.EasterEggHelp
import com.dede.android_eggs.main.entity.TimelineEvent
import com.dede.android_eggs.main.entity.TimelineEvent.Companion.isNewGroup
import com.dede.android_eggs.ui.drawables.AlterableAdaptiveIconDrawable
import com.dede.android_eggs.util.compose.PathShape
import com.dede.android_eggs.views.main.compose.DrawableImage
import com.dede.android_eggs.views.settings.compose.IconShapePrefUtil
import com.dede.basic.provider.EasterEgg
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@Composable
fun TimelineList(
    showSheetState: MutableState<Boolean>,
    viewModel: TimelineViewModel = viewModel()
) {
    var showSheet by showSheetState
    if (!showSheet) {
        return
    }

    var sheetExpanded by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        confirmValueChange = {
            sheetExpanded = it == SheetValue.Expanded
            true
        }
    )
    val paddingValues = WindowInsets.systemBars.asPaddingValues()
    val topPadding by animateDpAsState(
        targetValue = if (sheetExpanded)
            max(0.dp, (paddingValues.calculateTopPadding() - 16.dp))
        else
            0.dp,
        label = "ModalBottomSheet contentWindowInsetTop",
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
    )
    ModalBottomSheet(
        onDismissRequest = { showSheet = false },
        sheetState = sheetState,
        contentWindowInsets = {
            WindowInsets(0.dp, topPadding, 0.dp, 0.dp)
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = paddingValues.calculateBottomPadding())
        ) {
            item {
                TimelineHeader()
            }
            items(viewModel.timelines) {
                TimelineItem(
                    it,
                    viewModel.logoMatcher.findAndroidLogo(it.apiLevel),
                    viewModel.logoMatcher.findEasterEgg(it.apiLevel),
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun TimelineHeader() {
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val (logo, title) = createRefs()
        Icon(
            imageVector = Icons.Rounded.Android,
            contentDescription = null,
            modifier = Modifier
                .background(Color(0xFF1D1E21), CircleShape)
                .size(40.dp)
                .padding(6.dp)
                .constrainAs(logo) {
                    centerHorizontallyTo(parent, 0.3f)
                },
            tint = Color(0xFF35D779)
        )
        Text(
            text = stringResource(id = R.string.label_timeline),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(title) {
                linkTo(start = logo.end, end = parent.end, bias = 0f, startMargin = 10.dp)
                centerVerticallyTo(logo)
            }
        )
    }
}

@Composable
@Preview(showBackground = true)
private fun TimelineItem(
    event: TimelineEvent = TimelineEvent.timelines.first(),
    @DrawableRes logo: Int = R.mipmap.ic_launcher,
    egg: EasterEgg? = EasterEggHelp.previewEasterEggs().first(),
) {
    val context = LocalContext.current
    ConstraintLayout(
        modifier = Modifier.fillMaxWidth()
    ) {
        val (year, img, month, desc, line) = createRefs()

        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .constrainAs(line) {
                    height = Dimension.fillToConstraints
                    width = Dimension.value(2.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    centerHorizontallyTo(img)
                }
        )

        val iconShape = remember { IconShapePrefUtil.getMaskPath(context) }
        val drawable = remember(logo, context.theme) {
            AlterableAdaptiveIconDrawable(context, logo, iconShape)
        }
        val imageModifier = Modifier
            .size(40.dp)
            .constrainAs(img) {
                top.linkTo(parent.top, 16.dp)
                centerHorizontallyTo(parent, 0.3f)
            }
        val eggNickName = if (egg != null) {
            stringResource(id = egg.nicknameRes)
        } else {
            null
        }
        if (event.apiLevel >= Build.VERSION_CODES.LOLLIPOP) {
            DrawableImage(
                drawable = drawable,
                contentDescription = eggNickName,
                modifier = imageModifier
            )
        } else {
            DrawableImage(
                res = logo,
                contentDescription = eggNickName,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, PathShape(iconShape))
                    .then(imageModifier)
                    .padding(6.dp)
            )
        }
        if (event.isNewGroup()) {
            Text(
                text = event.localYear ?: "",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.constrainAs(year) {
                    end.linkTo(img.start, 12.dp)
                    centerVerticallyTo(img)
                }
            )
        }
        Text(
            text = event.localMonth ?: "",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.constrainAs(month) {
                start.linkTo(img.end, 12.dp)
                top.linkTo(img.top)
            }
        )
        Text(
            text = event.eventAnnotatedString,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(bottom = 12.dp)
                .constrainAs(desc) {
                    width = Dimension.fillToConstraints
                    linkTo(
                        start = month.start,
                        end = parent.end,
                        endMargin = 16.dp,
                        bias = 0f,
                    )
                    top.linkTo(month.bottom)
                }
        )
    }
}

@HiltViewModel
class TimelineViewModel @Inject constructor() : ViewModel() {
    val timelines: List<TimelineEvent> = TimelineEvent.timelines

    @Inject
    lateinit var logoMatcher: AndroidLogoMatcher
}
