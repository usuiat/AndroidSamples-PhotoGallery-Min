package net.engawapg.app.photogallery_min

import android.Manifest
import android.content.ContentUris
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.PermissionChecker
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_image_item.view.*

class MainActivity : AppCompatActivity() {
    private val imageUris = mutableListOf<Uri>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        /* RecyclerView */
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(this@MainActivity, 3)
            adapter = ImageAdapter()
        }
    }

    override fun onResume() {
        super.onResume()

        /*
        READ_EXTERNAL_STORAGEパーミッション取得済みかどうかの確認。
        READ_EXTERNAL_STORAGEは実行時に権限を要求する必要がある。
        簡単のため、パーミッション関係の実装は正常系のみとしている。
        */
        val result = PermissionChecker.checkSelfPermission(this,
            Manifest.permission.READ_EXTERNAL_STORAGE)

        /* パーミッション未取得なら、要求する。 */
        if (result != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE)
            return
        }

        /* パーミッション取得済みなら、画像を読み込む。*/
        loadImages()
    }

    /* MediaStoreから画像を読み込む */
    private fun loadImages() {
        imageUris.clear()

        /* 検索条件の設定。このサンプルはとにかく簡単にするため、すべてnull */
        val projection = null /* 読み込む列の指定。nullならすべての列を読み込む。*/
        val selection = null /* 行の絞り込みの指定。nullならすべての行を読み込む。*/
        val selectionArgs = null /* selectionの?を置き換える引数 */
        val sortOrder = null /* 並び順。nullなら指定なし。*/

        /*
        このサンプルではMediaStore以外のソースコードを極力少なくするため、メインスレッドで実行している。
        そのため、スクロールが頻繁に固まる。実際のアプリではバックグラウンドスレッドで実行すること。
        */
        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection, selection, selectionArgs, sortOrder
        )?.use { cursor -> /* cursorは、検索結果の各行の情報にアクセスするためのオブジェクト。*/
            /* 必要な情報が格納されている列番号を取得する。 */
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            while (cursor.moveToNext()) { /* 順にカーソルを動かしながら、情報を取得していく。*/
                val id = cursor.getLong(idColumn)
                /* IDからURIを取得してリストに格納 */
                val uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageUris.add(uri)
            }
        }

        /* 表示更新 */
        recyclerView.adapter?.notifyDataSetChanged()
    }

    companion object {
        const val REQUEST_CODE = 1
    }

    /* 以下はRecyclerView関連 */
    inner class ImageAdapter: RecyclerView.Adapter<ImageViewHolder>() {
        override fun getItemCount() = imageUris.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val view = layoutInflater.inflate(R.layout.view_image_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            holder.itemView.imageView.setImageURI(imageUris[position])
        }
    }

    inner class ImageViewHolder(v: View): RecyclerView.ViewHolder(v)
}