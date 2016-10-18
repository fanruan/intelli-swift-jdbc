/**
 * 文本控件
 * Created by Young's on 2016/10/13.
 */
import {Fetch} from 'lib'
import AbstractWidget from './AbstractWidget'

class StringControl extends AbstractWidget {
    constructor($widget, ...props) {
        super($widget, ...props);
    }

    isControl() {
        return true;
    }

    getYear() {
        return this.$widget.getIn(['value', 'year']);
    }

    getMonth() {
        return this.$widget.getIn(['value', 'month']);
    }

    getDay() {
        return this.$widget.getIn(['value', 'day']);
    }
}
export default StringControl;