/*
 *  Copyright 2020 Xiaomi
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package run.mone.mimeter.dashboard.bo.dataset;

import com.xiaomi.mone.http.docs.annotations.HttpApiDocClassDefine;
import lombok.Data;
import run.mone.mimeter.dashboard.bo.common.PageBase;

import java.io.Serializable;
import java.util.List;

@Data
public class DatasetList extends PageBase implements Serializable {


    /**
     * 返回的信息列表
     */
    @HttpApiDocClassDefine(value = "list", required = false, description = "数据源列表")
    private List<DatasetDto> list;

}
