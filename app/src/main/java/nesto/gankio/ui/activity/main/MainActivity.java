package nesto.gankio.ui.activity.main;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;

import butterknife.Bind;
import butterknife.ButterKnife;
import nesto.gankio.R;
import nesto.gankio.model.DataType;
import nesto.gankio.ui.activity.ActionBarActivity;
import nesto.gankio.ui.fragment.normal.NormalFragment;


public class MainActivity extends ActionBarActivity {

    @Bind(R.id.viewpager)
    ViewPager viewPager;

    @Bind(R.id.tabs)
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        showAnimation();
    }

    @Override
    public void setContentView() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void initForAnimation() {
        super.initForAnimation();
        tabLayout.setAlpha(0);
    }

    @Override
    protected void doAfterAnimation() {
        super.doAfterAnimation();
        ViewCompat.animate(tabLayout).alpha(1).start();
    }

    private void setupViewPager(ViewPager viewPager) {
        final MainFragmentAdapter adapter = new MainFragmentAdapter(getSupportFragmentManager());
        for (DataType type : DataType.values()) {
            adapter.addFragment(new NormalFragment().setType(type), type.toString());
        }
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (getSupportActionBar() != null) {
                    setTitle(adapter.getPageTitle(Math.round(position + positionOffset)));
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        viewPager.setCurrentItem(0);
    }
}
