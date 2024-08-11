/*
 * MIT License
 *
 * Stream-rec  https://github.com/hua0512/stream-rec
 *
 * Copyright (c) 2024 hua0512 (https://github.com/hua0512)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package github.hua0512.repo

import github.hua0512.dao.config.AppConfigDao
import github.hua0512.dao.user.UserDao
import github.hua0512.data.AppConfigId
import github.hua0512.data.config.AppConfig
import github.hua0512.data.user.UserEntity
import github.hua0512.logger
import github.hua0512.utils.md5
import github.hua0512.utils.withIOContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map

/**
 * Local data source implementation
 * @author hua0512
 * @date : 2024/2/18 23:55
 */
class LocalDataSourceImpl(private val dao: AppConfigDao, private val userDao: UserDao) : LocalDataSource {

  private companion object {
    private const val DEFAULT_PASSWORD = "stream-rec"
  }

  override suspend fun streamAppConfig(): Flow<AppConfig> {
    return dao.streamLatest()?.map {
      AppConfig(it)
    } ?: emptyFlow()
  }


  override suspend fun getAppConfig(): AppConfig = withIOContext {
    dao.getById(AppConfigId(1))?.let { AppConfig(it) } ?: AppConfig().apply {
      logger.info("First time running the app, creating default app config")
      val password = System.getenv("LOGIN_SECRET") ?: DEFAULT_PASSWORD
      val user = UserEntity(0, "stream-rec", password.md5(), "ADMIN", isActive = true)
      userDao.insert(user)
      saveAppConfig(this)
    }
  }

  override fun getPath() = LocalDataSource.getDefaultPath()

  override suspend fun saveAppConfig(appConfig: AppConfig) = dao.upsert(appConfig.toEntity())
}