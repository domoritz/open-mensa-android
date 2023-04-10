package de.uni_potsdam.hpi.openmensa.ui.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import de.uni_potsdam.hpi.openmensa.R
import de.uni_potsdam.hpi.openmensa.data.AppDatabase
import de.uni_potsdam.hpi.openmensa.helpers.DateUtils
import de.uni_potsdam.hpi.openmensa.ui.presentation.CanteenWithDays
import de.uni_potsdam.hpi.openmensa.ui.viewer.RelativeDate
import de.uni_potsdam.hpi.openmensa.ui.viewer.ViewerActivity

class MealWidgetService: RemoteViewsService() {
    companion object {
        private const val EXTRA_CANTEEN_ID = "canteenId"

        fun intent(context: Context, canteenId: Int) = Intent(context, MealWidgetService::class.java)
            .setData(Uri.fromParts("canteen", "$canteenId", null))
            .putExtra(EXTRA_CANTEEN_ID, canteenId)
    }

    internal sealed class ListItem
    internal data class CanteenHeader(val title: String): ListItem()
    internal data class DateItem(val date: String, val relativeDate: String): ListItem()
    internal data class MealItem(val text: String, val date: String, val id: Int): ListItem()
    internal object Bottom: ListItem()

    override fun onGetViewFactory(intent: Intent) = object: RemoteViewsFactory {
        val database = AppDatabase.with(application)
        val canteenId = intent.getIntExtra(EXTRA_CANTEEN_ID, -1)

        var data: List<ListItem> = emptyList()

        override fun onCreate() {
            // nothing to do
        }

        override fun onDataSetChanged() {
            val currentDate = DateUtils.formatWithLocalTimezone(System.currentTimeMillis())
            val canteen = CanteenWithDays.getSync(database, canteenId)
            val dates = canteen?.getDatesToShow(currentDate) ?: emptyList()
            val meals = database.meal.getByCanteenSync(canteenId)
            val mealsByDate = meals.groupBy { it.date }

            data = if (canteen != null) {
                val canteenHeader = CanteenHeader(canteen.canteen.name)

                listOf(canteenHeader) + dates.map { date ->
                    val dateMeals = mealsByDate[date] ?: emptyList()
                    val relativeDate = RelativeDate.format(currentDate, date)

                    val header = DateItem(date, relativeDate)
                    val items = dateMeals.map { MealItem(it.name, date, it.id) }

                    listOf(header) + items
                }.flatten() + Bottom
            } else emptyList()
        }

        override fun onDestroy() {
            // nothing to do
        }

        override fun getCount(): Int = data.size

        override fun getViewTypeCount(): Int = 4
        override fun getViewAt(index: Int): RemoteViews = when (val item = data[index]) {
            is CanteenHeader -> RemoteViews(packageName, R.layout.meal_widget_canteen_header).also { view ->
                view.setTextViewText(R.id.root, item.title)

                view.setOnClickFillInIntent(
                    R.id.root,
                    Intent()
                        .putExtra(ViewerActivity.EXTRA_CANTEEN_ID, canteenId)
                )
            }
            is DateItem -> RemoteViews(packageName, R.layout.meal_widget_date_header).also { view ->
                view.setTextViewText(R.id.root, item.relativeDate)

                view.setOnClickFillInIntent(
                    R.id.root,
                    Intent()
                        .putExtra(ViewerActivity.EXTRA_CANTEEN_ID, canteenId)
                        .putExtra(ViewerActivity.EXTRA_DATE, item.date)
                )
            }
            is MealItem -> RemoteViews(packageName, R.layout.meal_widget_meal_item).also { view ->
                view.setTextViewText(R.id.root, item.text)

                view.setOnClickFillInIntent(
                    R.id.root,
                    Intent()
                        .putExtra(ViewerActivity.EXTRA_CANTEEN_ID, canteenId)
                        .putExtra(ViewerActivity.EXTRA_DATE, item.date)
                        .putExtra(ViewerActivity.EXTRA_MEAL_ID, item.id)
                )
            }
            Bottom -> RemoteViews(packageName, R.layout.meal_widget_bottom_padding)
        }

        override fun getItemId(index: Int): Long = data[index].hashCode().toLong()
        override fun hasStableIds(): Boolean = false

        override fun getLoadingView(): RemoteViews? = null
    }
}