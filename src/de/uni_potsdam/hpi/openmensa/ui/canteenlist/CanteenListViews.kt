package de.uni_potsdam.hpi.openmensa.ui.canteenlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.uni_potsdam.hpi.openmensa.R
import kotlinx.coroutines.launch

object CanteenListViews {
    @Composable
    fun View(content: CanteenListModel.Screen) {
        when (content) {
            is CanteenListModel.Screen.None -> {/* nothing to do */}
            is CanteenListModel.Screen.Dialog -> {
                CanteenList(content.canteen)

                if (content.city != null) CityList(content.city)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun CanteenList(content: CanteenListModel.Screen.CanteenList) {
        val coroutineScope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState()
        val lazyListState = rememberLazyListState()

        ModalBottomSheet(
            onDismissRequest = content.actions.cancel,
            sheetState = sheetState,
            content = {
                LazyColumn (state = lazyListState) {
                    item {
                        SearchField(
                            isVisible = content.mode is CanteenListModel.Screen.CanteenList.Big,
                            value = when (content.mode) {
                                is CanteenListModel.Screen.CanteenList.Small -> ""
                                is CanteenListModel.Screen.CanteenList.Big -> content.mode.searchTerm
                            },
                            onValueChange = content.actions.updateSearchTerm
                        )
                    }

                    items(content.items, key = { it.canteen.id }) { item ->
                        SmallCanteenListItem(
                            when (item.reason) {
                                CanteenListModel.CanteenReason.Distance -> Icons.Default.LocationOn
                                CanteenListModel.CanteenReason.Favorite -> Icons.Default.Star
                                CanteenListModel.CanteenReason.Listing -> Icons.Default.Restaurant
                            },
                            item.canteen.name,
                            { content.actions.pickCanteen(item) },
                            Modifier.animateItemPlacement()
                        )
                    }

                    item {
                        GrantLocationAccessItem(
                            isVisible = content.mode is CanteenListModel.Screen.CanteenList.Small &&
                                    content.mode.isMissingLocationAccess,
                            action = content.actions.requestLocationAccess
                        )
                    }

                    item {
                        ShowAllItem(
                            isVisible = content.mode is CanteenListModel.Screen.CanteenList.Small &&
                                    content.mode.hasMoreCanteens,
                            action = {
                                content.actions.showAllCanteens()

                                coroutineScope.launch {
                                    sheetState.expand()
                                    lazyListState.animateScrollToItem(0)
                                    sheetState.expand()
                                }
                            }
                        )
                    }

                    item {
                        SmallCanteenListItem(
                            Icons.Default.LocationCity,
                            stringResource(R.string.canteen_list_select_city),
                            content.actions.switchCity
                        )
                    }
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
    @Composable
    private fun CityList(content: CanteenListModel.Screen.CityList) {
        val coroutineScope = rememberCoroutineScope()
        val sheetState = rememberModalBottomSheetState()
        val lazyListState = rememberLazyListState()

        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                content.actions.cancel()
            },
            content = {
                LazyColumn (state = lazyListState) {
                    item {
                        SearchField(
                            isVisible = content.mode is CanteenListModel.Screen.CityList.Big,
                            value = when (content.mode) {
                                is CanteenListModel.Screen.CityList.Small -> ""
                                is CanteenListModel.Screen.CityList.Big -> content.mode.searchTerm
                            },
                            onValueChange = content.actions.updateSearchTerm
                        )
                    }

                    items(content.items, key = { it.city }) { item ->
                        SmallCanteenListItem(
                            when (item.reason) {
                                CanteenListModel.CityReason.History -> Icons.Default.History
                                CanteenListModel.CityReason.Distance -> Icons.Default.LocationOn
                                CanteenListModel.CityReason.Listing -> Icons.Default.LocationCity
                            },
                            item.city,
                            { content.actions.pickCity(item) },
                            Modifier.animateItemPlacement()
                        )
                    }

                    item {
                        GrantLocationAccessItem(
                            isVisible = content.mode is CanteenListModel.Screen.CityList.Small &&
                                    content.mode.isMissingLocationAccess,
                            action = content.actions.requestLocationAccess
                        )
                    }

                    item {
                        ShowAllItem(
                            isVisible = content.mode is CanteenListModel.Screen.CityList.Small,
                            action = {
                                content.actions.showAllCities()

                                coroutineScope.launch {
                                    sheetState.expand()
                                    lazyListState.animateScrollToItem(0)
                                    sheetState.expand()
                                }
                            }
                        )
                    }
                }
            }
        )
    }

    @Composable
    private fun SearchField(
        isVisible: Boolean,
        value: String,
        onValueChange: (String) -> Unit
    ) {
        AnimatedVisibility(isVisible) {
            TextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                label = {
                    Text(stringResource(R.string.select_canteen_dialog_search_hint))
                },
                trailingIcon = {
                    Icon(
                        Icons.Default.Search,
                        stringResource(R.string.select_canteen_dialog_search_hint)
                    )
                }
            )
        }
    }

    @Composable
    private fun GrantLocationAccessItem(
        isVisible: Boolean,
        action: () -> Unit
    ) {
        AnimatedVisibility(isVisible) {
            SmallCanteenListItem(
                Icons.Default.LocationOn,
                stringResource(R.string.canteen_list_enable_loc_access),
                action
            )
        }
    }

    @Composable
    private fun ShowAllItem(
        isVisible: Boolean,
        action: () -> Unit
    ) {
        AnimatedVisibility (isVisible) {
            SmallCanteenListItem(
                Icons.Default.UnfoldMore,
                stringResource(R.string.canteen_list_show_more),
                action
            )
        }
    }

    @Composable
    private fun SmallCanteenListItem(
        icon: ImageVector,
        text: String,
        handler: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row (
            modifier = modifier
                .fillMaxWidth()
                .clickable { handler() }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, text)
            Text(text)
        }
    }
}